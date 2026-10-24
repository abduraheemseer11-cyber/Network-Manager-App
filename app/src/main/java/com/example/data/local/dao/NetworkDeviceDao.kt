package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.NetworkDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDeviceDao {
    @Query("SELECT * FROM network_devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<NetworkDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: NetworkDevice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<NetworkDevice>)

    @Update
    suspend fun updateDevice(device: NetworkDevice)

    @Delete
    suspend fun deleteDevice(device: NetworkDevice)
}
