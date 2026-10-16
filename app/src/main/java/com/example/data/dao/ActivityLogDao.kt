package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ActivityLogEntity
import com.example.data.entity.GoalCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: GoalCategory): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE category = :category AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getLogsByCategoryBetween(category: GoalCategory, startTime: Long, endTime: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ActivityLogEntity>): List<Long>

    @Delete
    suspend fun deleteLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun getLogCount(): Int
}
