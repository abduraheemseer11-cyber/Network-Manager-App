package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_devices")
data class NetworkDevice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "ROUTER", "ACCESS_POINT", "SECTOR_ANTENNA", "SWITCH"
    val latitude: Double,
    val longitude: Double,
    val status: String = "ONLINE", // "ONLINE", "OFFLINE", "WARNING"
    val ipAddress: String = "",
    val connectedClients: Int = 0,
    val notes: String = ""
)
