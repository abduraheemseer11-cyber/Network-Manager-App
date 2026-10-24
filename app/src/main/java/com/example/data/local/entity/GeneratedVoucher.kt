package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_vouchers")
data class GeneratedVoucher(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: String,
    val username: String,
    val password: String,
    val profileName: String,
    val timeLimit: String = "",
    val dataLimit: String = "",
    val price: String = "",
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPrinted: Boolean = false
)
