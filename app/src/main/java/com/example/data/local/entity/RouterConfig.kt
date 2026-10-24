package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "router_configs")
data class RouterConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ipAddress: String,
    val apiPort: Int = 8728,
    val username: String = "admin",
    val password: String = "",
    val hotspotName: String = "شبكة ميكروتك",
    val dnsName: String = "mikrotik.net",
    val currency: String = "ر.ي",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
