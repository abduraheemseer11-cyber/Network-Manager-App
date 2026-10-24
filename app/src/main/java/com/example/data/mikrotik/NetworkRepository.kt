package com.example.data.mikrotik

import com.example.data.local.dao.NetworkDeviceDao
import com.example.data.local.dao.RouterDao
import com.example.data.local.dao.VoucherDao
import com.example.data.local.entity.GeneratedVoucher
import com.example.data.local.entity.NetworkDevice
import com.example.data.local.entity.RouterConfig
import com.example.data.model.HotspotActiveUser
import com.example.data.model.HotspotUser
import com.example.data.model.SystemResource
import com.example.data.model.TrafficStats
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class NetworkRepository(
    private val routerDao: RouterDao,
    private val voucherDao: VoucherDao,
    private val networkDeviceDao: NetworkDeviceDao,
    private val coroutineScope: CoroutineScope
) {
    private val apiClient = RouterOsApiClient()

    val allRouters: Flow<List<RouterConfig>> = routerDao.getAllRouters()
    val activeRouterConfig: Flow<RouterConfig?> = routerDao.getActiveRouter()
    val allVouchers: Flow<List<GeneratedVoucher>> = voucherDao.getAllVouchers()
    val allNetworkDevices: Flow<List<NetworkDevice>> = networkDeviceDao.getAllDevices()

    private val _isLiveConnected = MutableStateFlow(false)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _connectionStatusMessage = MutableStateFlow("جاري فحص الاتصال بالراوتر...")
    val connectionStatusMessage: StateFlow<String> = _connectionStatusMessage.asStateFlow()

    private val _systemResource = MutableStateFlow(SystemResource())
    val systemResource: StateFlow<SystemResource> = _systemResource.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    private val _hotspotUsers = MutableStateFlow<List<HotspotUser>>(emptyList())
    val hotspotUsers: StateFlow<List<HotspotUser>> = _hotspotUsers.asStateFlow()

    private val _activeUsers = MutableStateFlow<List<HotspotActiveUser>>(emptyList())
    val activeUsers: StateFlow<List<HotspotActiveUser>> = _activeUsers.asStateFlow()

    private val _profiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

    init {
        // Initialize default profiles
        _profiles.value = listOf(
            UserProfile(id = "*1", name = "باقة_ساعة_سريعة", rateLimit = "2M/4M", validity = "1h", price = "100", sellingPrice = "100"),
            UserProfile(id = "*2", name = "باقة_يومية_500ميجابايت", rateLimit = "1M/3M", validity = "1d", price = "250", sellingPrice = "250"),
            UserProfile(id = "*3", name = "باقة_أسبوعية_3جيجابايت", rateLimit = "2M/5M", validity = "7d", price = "1000", sellingPrice = "1000"),
            UserProfile(id = "*4", name = "باقة_شهرية_VIP_غير_محدود", rateLimit = "4M/10M", validity = "30d", price = "3500", sellingPrice = "3500")
        )

        // Seed initial interactive mock users
        _hotspotUsers.value = listOf(
            HotspotUser(id = "*1", name = "user712", password = "712", profile = "باقة_يومية_500ميجابايت", uptime = "3h 12m", bytesIn = 142_000_000, bytesOut = 28_000_000, comment = "كارت 500ميجابايت", price = "250"),
            HotspotUser(id = "*2", name = "vip_ahmed", password = "994", profile = "باقة_شهرية_VIP_غير_محدود", uptime = "4d 18h", bytesIn = 1_840_000_000, bytesOut = 430_000_000, comment = "مشترك دائم", price = "3500"),
            HotspotUser(id = "*3", name = "guest_44", password = "44", profile = "باقة_ساعة_سريعة", uptime = "42m", bytesIn = 65_000_000, bytesOut = 9_000_000, comment = "زائر المحل", price = "100"),
            HotspotUser(id = "*4", name = "card_8912", password = "8912", profile = "باقة_أسبوعية_3جيجابايت", uptime = "1d 06h", bytesIn = 890_000_000, bytesOut = 120_000_000, comment = "دفعة 10-2026", price = "1000"),
            HotspotUser(id = "*5", name = "user883", password = "883", profile = "باقة_يومية_500ميجابايت", uptime = "0s", bytesIn = 0, bytesOut = 0, comment = "جديد غير مستخدم", price = "250")
        )

        _activeUsers.value = listOf(
            HotspotActiveUser(id = "*A1", user = "user712", address = "192.168.88.145", macAddress = "6C:29:95:A1:33:EF", uptime = "3h 12m", bytesIn = 142_000_000, bytesOut = 28_000_000),
            HotspotActiveUser(id = "*A2", user = "vip_ahmed", address = "192.168.88.160", macAddress = "D4:61:9D:44:B2:10", uptime = "4d 18h", bytesIn = 1_840_000_000, bytesOut = 430_000_000),
            HotspotActiveUser(id = "*A3", user = "guest_44", address = "192.168.88.172", macAddress = "AC:5F:3E:99:11:8A", uptime = "42m", bytesIn = 65_000_000, bytesOut = 9_000_000)
        )

        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2000)
                // Real-time animated traffic fluctuations
                val baseRx = if (_isLiveConnected.value) 22_000_000L else 14_000_000L
                val baseTx = if (_isLiveConnected.value) 6_500_000L else 4_200_000L
                val jitterRx = Random.nextLong(-3_000_000L, 4_000_000L)
                val jitterTx = Random.nextLong(-1_000_000L, 1_500_000L)

                _trafficStats.value = TrafficStats(
                    interfaceName = "ether1-WAN",
                    rxBps = (baseRx + jitterRx).coerceAtLeast(500_000L),
                    txBps = (baseTx + jitterTx).coerceAtLeast(200_000L)
                )

                val cpu = (Random.nextInt(12, 38)).coerceIn(5, 95)
                val current = _systemResource.value
                _systemResource.value = current.copy(cpuLoad = cpu)
            }
        }
    }

    suspend fun connectToRouter(config: RouterConfig) {
        _isConnecting.value = true
        _connectionStatusMessage.value = "جاري الاتصال بـ ${config.name} (${config.ipAddress}:${config.apiPort})..."
        try {
            val result = apiClient.connect(
                host = config.ipAddress,
                port = config.apiPort,
                user = config.username,
                pass = config.password,
                timeoutMs = 3500
            )

            if (result.isSuccess) {
                _isLiveConnected.value = true
                _connectionStatusMessage.value = "متصل بنجاح مع الراوتر ${config.name}"
                fetchLiveRouterData()
            } else {
                _isLiveConnected.value = false
                _connectionStatusMessage.value = "وضع المحاكاة المباشر (تعذر الاتصال بـ ${config.ipAddress}:${config.apiPort} - تأكد من فتح منفذ API 8728)"
            }
        } catch (e: Exception) {
            _isLiveConnected.value = false
            _connectionStatusMessage.value = "وضع المحاكاة المباشر (${e.localizedMessage ?: "الراوتر غير متاح على الشبكة المحلية"})"
        } finally {
            _isConnecting.value = false
        }
    }

    private suspend fun fetchLiveRouterData() {
        if (!_isLiveConnected.value) return
        try {
            // Fetch Resource
            val res = apiClient.executeCommand("/system/resource/print")
            val resMap = parseSentenceMap(res)
            if (resMap.isNotEmpty()) {
                _systemResource.value = SystemResource(
                    boardName = resMap["board-name"] ?: "MikroTik",
                    version = resMap["version"] ?: "RouterOS",
                    uptime = resMap["uptime"] ?: "0s",
                    cpuLoad = resMap["cpu-load"]?.toIntOrNull() ?: 15,
                    freeMemoryMb = (resMap["free-memory"]?.toLongOrNull() ?: 64_000_000) / (1024 * 1024),
                    totalMemoryMb = (resMap["total-memory"]?.toLongOrNull() ?: 128_000_000) / (1024 * 1024),
                    freeHddMb = (resMap["free-hdd-space"]?.toLongOrNull() ?: 64_000_000) / (1024 * 1024),
                    totalHddMb = (resMap["total-hdd-space"]?.toLongOrNull() ?: 128_000_000) / (1024 * 1024)
                )
            }

            // Fetch Profiles
            val profSentences = apiClient.executeCommand("/ip/hotspot/user/profile/print")
            val parsedProfiles = profSentences.filter { it.firstOrNull() == "!re" }.map { sentence ->
                val map = sentenceToMap(sentence)
                UserProfile(
                    id = map[".id"] ?: UUID.randomUUID().toString(),
                    name = map["name"] ?: "default",
                    rateLimit = map["rate-limit"] ?: "غير محدد",
                    sharedUsers = map["shared-users"]?.toIntOrNull() ?: 1
                )
            }
            if (parsedProfiles.isNotEmpty()) {
                _profiles.value = parsedProfiles
            }

            // Fetch Users
            val userSentences = apiClient.executeCommand("/ip/hotspot/user/print")
            val parsedUsers = userSentences.filter { it.firstOrNull() == "!re" }.map { sentence ->
                val map = sentenceToMap(sentence)
                HotspotUser(
                    id = map[".id"] ?: UUID.randomUUID().toString(),
                    name = map["name"] ?: "",
                    password = map["password"] ?: "",
                    profile = map["profile"] ?: "default",
                    uptime = map["uptime"] ?: "0s",
                    bytesIn = map["bytes-in"]?.toLongOrNull() ?: 0L,
                    bytesOut = map["bytes-out"]?.toLongOrNull() ?: 0L,
                    comment = map["comment"] ?: "",
                    disabled = map["disabled"] == "true"
                )
            }
            if (parsedUsers.isNotEmpty()) {
                _hotspotUsers.value = parsedUsers
            }

            // Fetch Active Users
            val activeSentences = apiClient.executeCommand("/ip/hotspot/active/print")
            val parsedActive = activeSentences.filter { it.firstOrNull() == "!re" }.map { sentence ->
                val map = sentenceToMap(sentence)
                HotspotActiveUser(
                    id = map[".id"] ?: UUID.randomUUID().toString(),
                    user = map["user"] ?: "",
                    address = map["address"] ?: "",
                    macAddress = map["mac-address"] ?: "",
                    uptime = map["uptime"] ?: "0s",
                    bytesIn = map["bytes-in"]?.toLongOrNull() ?: 0L,
                    bytesOut = map["bytes-out"]?.toLongOrNull() ?: 0L
                )
            }
            _activeUsers.value = parsedActive
        } catch (_: Exception) {}
    }

    private fun parseSentenceMap(sentences: List<List<String>>): Map<String, String> {
        val re = sentences.firstOrNull { it.firstOrNull() == "!re" } ?: return emptyMap()
        return sentenceToMap(re)
    }

    private fun sentenceToMap(sentence: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (item in sentence) {
            if (item.startsWith("=")) {
                val pair = item.substring(1)
                val eqIdx = pair.indexOf('=')
                if (eqIdx > 0) {
                    val k = pair.substring(0, eqIdx)
                    val v = pair.substring(eqIdx + 1)
                    map[k] = v
                }
            }
        }
        return map
    }

    // CRUD Hotspot Users
    suspend fun addHotspotUser(
        name: String,
        pass: String,
        profile: String,
        timeLimit: String,
        dataLimitBytes: Long,
        comment: String,
        price: String = ""
    ) {
        if (_isLiveConnected.value) {
            try {
                val params = mutableMapOf(
                    "name" to name,
                    "password" to pass,
                    "profile" to profile
                )
                if (timeLimit.isNotEmpty()) params["limit-uptime"] = timeLimit
                if (dataLimitBytes > 0) params["limit-bytes-total"] = dataLimitBytes.toString()
                if (comment.isNotEmpty()) params["comment"] = comment
                apiClient.executeCommand("/ip/hotspot/user/add", params)
                fetchLiveRouterData()
            } catch (_: Exception) {}
        }

        // Always update local state
        val newUser = HotspotUser(
            id = "*local_${System.currentTimeMillis()}",
            name = name,
            password = pass,
            profile = profile,
            uptime = "0s",
            comment = comment,
            price = price
        )
        _hotspotUsers.value = listOf(newUser) + _hotspotUsers.value
    }

    suspend fun removeHotspotUser(user: HotspotUser) {
        if (_isLiveConnected.value) {
            try {
                apiClient.executeCommand("/ip/hotspot/user/remove", mapOf(".id" to user.id))
            } catch (_: Exception) {}
        }
        _hotspotUsers.value = _hotspotUsers.value.filter { it.id != user.id }
    }

    suspend fun toggleUserDisabled(user: HotspotUser) {
        val newStatus = !user.disabled
        if (_isLiveConnected.value) {
            try {
                val cmd = if (newStatus) "/ip/hotspot/user/disable" else "/ip/hotspot/user/enable"
                apiClient.executeCommand(cmd, mapOf(".id" to user.id))
            } catch (_: Exception) {}
        }
        _hotspotUsers.value = _hotspotUsers.value.map {
            if (it.id == user.id) it.copy(disabled = newStatus) else it
        }
    }

    suspend fun resetUserCounters(user: HotspotUser) {
        if (_isLiveConnected.value) {
            try {
                apiClient.executeCommand("/ip/hotspot/user/reset-counters", mapOf(".id" to user.id))
            } catch (_: Exception) {}
        }
        _hotspotUsers.value = _hotspotUsers.value.map {
            if (it.id == user.id) it.copy(bytesIn = 0, bytesOut = 0, uptime = "0s") else it
        }
    }

    suspend fun disconnectActiveUser(activeUser: HotspotActiveUser) {
        if (_isLiveConnected.value) {
            try {
                apiClient.executeCommand("/ip/hotspot/active/remove", mapOf(".id" to activeUser.id))
            } catch (_: Exception) {}
        }
        _activeUsers.value = _activeUsers.value.filter { it.id != activeUser.id }
    }

    // Batch Voucher Generator (Core Mikhmon Feature)
    suspend fun generateBatchVouchers(
        quantity: Int,
        userMode: String, // "same" (user=pass) or "different"
        prefix: String,
        length: Int,
        profile: UserProfile,
        timeLimit: String,
        dataLimit: String,
        comment: String
    ): List<GeneratedVoucher> {
        val batchId = "BATCH_${System.currentTimeMillis()}"
        val vouchers = mutableListOf<GeneratedVoucher>()
        val characters = "23456789abcdefghjkmnpqrstuvwxyz"

        for (i in 1..quantity) {
            val randomBody = (1..length).map { characters.random() }.joinToString("")
            val username = if (prefix.isNotEmpty()) "$prefix$randomBody" else randomBody
            val password = if (userMode == "same") username else (1..length).map { characters.random() }.joinToString("")

            val voucher = GeneratedVoucher(
                batchId = batchId,
                username = username,
                password = password,
                profileName = profile.name,
                timeLimit = timeLimit.ifEmpty { profile.validity },
                dataLimit = dataLimit,
                price = profile.price,
                comment = comment.ifEmpty { "دفعة $batchId" }
            )
            vouchers.add(voucher)

            // Add to router users list
            addHotspotUser(
                name = username,
                pass = password,
                profile = profile.name,
                timeLimit = voucher.timeLimit,
                dataLimitBytes = 0L,
                comment = voucher.comment,
                price = voucher.price
            )
        }

        voucherDao.insertVouchers(vouchers)
        return vouchers
    }

    // Add Profile
    suspend fun addProfile(profile: UserProfile) {
        if (_isLiveConnected.value) {
            try {
                apiClient.executeCommand(
                    "/ip/hotspot/user/profile/add",
                    mapOf(
                        "name" to profile.name,
                        "rate-limit" to profile.rateLimit,
                        "shared-users" to profile.sharedUsers.toString()
                    )
                )
            } catch (_: Exception) {}
        }
        _profiles.value = _profiles.value + profile
    }

    // Reboot Router
    suspend fun rebootRouter(): Boolean {
        if (_isLiveConnected.value) {
            try {
                apiClient.executeCommand("/system/reboot")
                _isLiveConnected.value = false
                return true
            } catch (_: Exception) {
                return false
            }
        }
        return true
    }

    // Routers Management
    suspend fun saveRouter(router: RouterConfig): Long {
        val id = routerDao.insertRouter(router)
        if (router.isActive) {
            routerDao.setActiveRouter(if (router.id != 0L) router.id else id)
        }
        return id
    }

    suspend fun deleteRouter(router: RouterConfig) {
        routerDao.deleteRouter(router)
    }

    suspend fun selectActiveRouter(router: RouterConfig) {
        routerDao.setActiveRouter(router.id)
        connectToRouter(router)
    }

    // Map Devices
    suspend fun saveNetworkDevice(device: NetworkDevice) {
        if (device.id == 0L) {
            networkDeviceDao.insertDevice(device)
        } else {
            networkDeviceDao.updateDevice(device)
        }
    }

    suspend fun deleteNetworkDevice(device: NetworkDevice) {
        networkDeviceDao.deleteDevice(device)
    }
}
