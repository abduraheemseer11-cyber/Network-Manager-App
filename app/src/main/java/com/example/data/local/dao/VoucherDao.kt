package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GeneratedVoucher
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM generated_vouchers ORDER BY createdAt DESC")
    fun getAllVouchers(): Flow<List<GeneratedVoucher>>

    @Query("SELECT * FROM generated_vouchers WHERE batchId = :batchId ORDER BY id ASC")
    suspend fun getVouchersByBatch(batchId: String): List<GeneratedVoucher>

    @Query("SELECT DISTINCT batchId FROM generated_vouchers ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<GeneratedVoucher>)

    @Query("DELETE FROM generated_vouchers WHERE batchId = :batchId")
    suspend fun deleteBatch(batchId: String)

    @Delete
    suspend fun deleteVoucher(voucher: GeneratedVoucher)

    @Query("DELETE FROM generated_vouchers")
    suspend fun clearAll()
}
