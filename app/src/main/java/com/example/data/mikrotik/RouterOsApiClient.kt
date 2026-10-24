package com.example.data.mikrotik

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Native MikroTik RouterOS API Client implementing binary wire protocol over TCP port 8728.
 */
class RouterOsApiClient {

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    suspend fun connect(
        host: String,
        port: Int = 8728,
        user: String = "admin",
        pass: String = "",
        timeoutMs: Int = 4000
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            disconnect()
            val s = Socket()
            s.connect(InetSocketAddress(host, port), timeoutMs)
            s.soTimeout = timeoutMs
            socket = s
            inputStream = s.getInputStream()
            outputStream = s.getOutputStream()

            // RouterOS v6.43+ supports login with name and password directly
            val loginSentences = executeCommand(
                "/login",
                mapOf("name" to user, "password" to pass)
            )

            val isDone = loginSentences.any { it.firstOrNull() == "!done" }
            if (isDone) {
                // Check if challenge is returned for older RouterOS versions
                val doneSentence = loginSentences.first { it.firstOrNull() == "!done" }
                val challenge = doneSentence.find { it.startsWith("=ret=") }?.removePrefix("=ret=")
                if (!challenge.isNullOrEmpty()) {
                    // Legacy MD5 Challenge Login
                    val responseHash = computeLegacyHash(pass, challenge)
                    val secondSentence = executeCommand(
                        "/login",
                        mapOf("name" to user, "response" to responseHash)
                    )
                    if (secondSentence.any { it.firstOrNull() == "!done" }) {
                        return@withContext Result.success(true)
                    } else {
                        return@withContext Result.failure(Exception("فشل تسجيل الدخول بتحدي كلمة المرور"))
                    }
                }
                return@withContext Result.success(true)
            } else {
                val errorMsg = loginSentences.flatten().find { it.startsWith("=message=") }
                    ?.removePrefix("=message=") ?: "فشل تسجيل الدخول للراوتر"
                return@withContext Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            disconnect()
            Result.failure(e)
        }
    }

    suspend fun executeCommand(
        command: String,
        params: Map<String, String> = emptyMap()
    ): List<List<String>> = withContext(Dispatchers.IO) {
        val os = outputStream ?: throw IllegalStateException("الراوتر غير متصل")
        val ins = inputStream ?: throw IllegalStateException("الراوتر غير متصل")

        val words = mutableListOf(command)
        params.forEach { (k, v) ->
            words.add("=$k=$v")
        }

        // Send words
        for (w in words) {
            writeWord(os, w)
        }
        // End of sentence
        os.write(0)
        os.flush()

        // Read response sentences until !done or !trap
        val sentences = mutableListOf<List<String>>()
        var currentSentence = mutableListOf<String>()

        while (true) {
            val word = readWord(ins)
            if (word == null) {
                if (currentSentence.isNotEmpty()) {
                    sentences.add(currentSentence)
                    val status = currentSentence.firstOrNull()
                    if (status == "!done" || status == "!trap") {
                        break
                    }
                    currentSentence = mutableListOf()
                }
            } else {
                currentSentence.add(word)
            }
        }

        sentences
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true && socket?.isClosed == false
    }

    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (_: Exception) {}
        socket = null
        inputStream = null
        outputStream = null
    }

    private fun writeWord(os: OutputStream, word: String) {
        val bytes = word.toByteArray(StandardCharsets.UTF_8)
        val len = bytes.size
        when {
            len < 0x80 -> {
                os.write(len)
            }
            len < 0x4000 -> {
                os.write((len shr 8) or 0x80)
                os.write(len and 0xFF)
            }
            len < 0x200000 -> {
                os.write((len shr 16) or 0xC0)
                os.write((len shr 8) and 0xFF)
                os.write(len and 0xFF)
            }
            len < 0x10000000 -> {
                os.write((len shr 24) or 0xE0)
                os.write((len shr 16) and 0xFF)
                os.write((len shr 8) and 0xFF)
                os.write(len and 0xFF)
            }
            else -> {
                os.write(0xF0)
                os.write((len shr 24) and 0xFF)
                os.write((len shr 16) and 0xFF)
                os.write((len shr 8) and 0xFF)
                os.write(len and 0xFF)
            }
        }
        os.write(bytes)
    }

    private fun readWord(ins: InputStream): String? {
        val len = readLength(ins)
        if (len <= 0) return null
        val buffer = ByteArray(len)
        var readSoFar = 0
        while (readSoFar < len) {
            val r = ins.read(buffer, readSoFar, len - readSoFar)
            if (r == -1) break
            readSoFar += r
        }
        return String(buffer, 0, readSoFar, StandardCharsets.UTF_8)
    }

    private fun readLength(ins: InputStream): Int {
        val b1 = ins.read()
        if (b1 == -1 || b1 == 0) return 0
        return when {
            (b1 and 0x80) == 0 -> b1
            (b1 and 0xC0) == 0x80 -> {
                val b2 = ins.read()
                ((b1 and 0x3F) shl 8) or b2
            }
            (b1 and 0xE0) == 0xC0 -> {
                val b2 = ins.read()
                val b3 = ins.read()
                ((b1 and 0x1F) shl 16) or (b2 shl 8) or b3
            }
            (b1 and 0xF0) == 0xE0 -> {
                val b2 = ins.read()
                val b3 = ins.read()
                val b4 = ins.read()
                ((b1 and 0x0F) shl 24) or (b2 shl 16) or (b3 shl 8) or b4
            }
            else -> {
                val b2 = ins.read()
                val b3 = ins.read()
                val b4 = ins.read()
                val b5 = ins.read()
                (b2 shl 24) or (b3 shl 16) or (b4 shl 8) or b5
            }
        }
    }

    private fun computeLegacyHash(password: String, challengeHex: String): String {
        val challengeBytes = hexStringToByteArray(challengeHex)
        val md = MessageDigest.getInstance("MD5")
        md.update(0.toByte())
        md.update(password.toByteArray(StandardCharsets.UTF_8))
        md.update(challengeBytes)
        val digest = md.digest()
        return "00" + digest.joinToString("") { "%02x".format(it) }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
