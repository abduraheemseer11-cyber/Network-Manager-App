package com.example.data.model

data class HotspotUser(
    val id: String,
    val name: String,
    val password: String = "",
    val profile: String = "default",
    val uptime: String = "0s",
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L,
    val comment: String = "",
    val disabled: Boolean = false,
    val limitUptime: String = "",
    val limitBytesTotal: Long = 0L,
    val price: String = ""
) {
    val totalBytes: Long get() = bytesIn + bytesOut

    fun formattedTotalBytes(): String = formatBytes(totalBytes)
    fun formattedBytesIn(): String = formatBytes(bytesIn)
    fun formattedBytesOut(): String = formatBytes(bytesOut)
}

data class HotspotActiveUser(
    val id: String,
    val user: String,
    val address: String,
    val macAddress: String,
    val uptime: String,
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L,
    val loginBy: String = "http"
) {
    fun formattedBytes(): String = formatBytes(bytesIn + bytesOut)
}

data class UserProfile(
    val id: String,
    val name: String,
    val rateLimit: String = "2M/5M",
    val sharedUsers: Int = 1,
    val validity: String = "1d",
    val price: String = "500",
    val sellingPrice: String = "500",
    val expiryMode: String = "remove", // remove, notice, rem_rec
    val lockUser: String = "Enable"
)

data class SystemResource(
    val boardName: String = "RouterBOARD 951Ui-2HnD",
    val version: String = "RouterOS v7.14",
    val uptime: String = "12d 04:32:15",
    val cpuLoad: Int = 18,
    val freeMemoryMb: Long = 84,
    val totalMemoryMb: Long = 128,
    val freeHddMb: Long = 92,
    val totalHddMb: Long = 128,
    val cpuCount: Int = 1,
    val cpuFrequency: Int = 600,
    val voltage: String = "24.2 V",
    val temperature: String = "39 C"
)

data class TrafficStats(
    val interfaceName: String = "ether1-WAN",
    val rxBps: Long = 14_500_000L, // 14.5 Mbps
    val txBps: Long = 4_200_000L   // 4.2 Mbps
) {
    fun formattedRx(): String = formatBits(rxBps)
    fun formattedTx(): String = formatBits(txBps)
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var digitGroups = 0
    var temp = bytes.toDouble()
    while (temp >= 1024 && digitGroups < units.size - 1) {
        temp /= 1024
        digitGroups++
    }
    return String.format("%.1f %s", temp, units[digitGroups])
}

fun formatBits(bps: Long): String {
    if (bps <= 0) return "0 bps"
    val units = arrayOf("bps", "Kbps", "Mbps", "Gbps")
    var digitGroups = 0
    var temp = bps.toDouble()
    while (temp >= 1000 && digitGroups < units.size - 1) {
        temp /= 1000
        digitGroups++
    }
    return String.format("%.1f %s", temp, units[digitGroups])
}
