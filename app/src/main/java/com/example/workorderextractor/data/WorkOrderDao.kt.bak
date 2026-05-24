package com.example.workorderextractor.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {
    @Insert
    suspend fun insert(order: WorkOrder)

    @Update
    suspend fun update(order: WorkOrder)

    @Delete
    suspend fun delete(order: WorkOrder)

    @Query("SELECT * FROM work_orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE jobId LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%'")
    fun searchOrders(query: String): Flow<List<WorkOrder>>
}
