package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RouterConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterDao {
    @Query("SELECT * FROM router_configs ORDER BY createdAt DESC")
    fun getAllRouters(): Flow<List<RouterConfig>>

    @Query("SELECT * FROM router_configs WHERE isActive = 1 LIMIT 1")
    fun getActiveRouter(): Flow<RouterConfig?>

    @Query("SELECT * FROM router_configs WHERE id = :id")
    suspend fun getRouterById(id: Long): RouterConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: RouterConfig): Long

    @Update
    suspend fun updateRouter(router: RouterConfig)

    @Delete
    suspend fun deleteRouter(router: RouterConfig)

    @Query("UPDATE router_configs SET isActive = CASE WHEN id = :routerId THEN 1 ELSE 0 END")
    suspend fun setActiveRouter(routerId: Long)
}
