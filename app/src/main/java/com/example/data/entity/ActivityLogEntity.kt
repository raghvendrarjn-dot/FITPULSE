package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long? = null,
    val category: GoalCategory,
    val activityName: String,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
