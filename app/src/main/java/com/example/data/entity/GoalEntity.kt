package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalCategory(val displayName: String, val defaultUnit: String, val defaultColorHex: String) {
    STEPS("Steps", "steps", "#00E5FF"),
    CALORIES("Calories", "kcal", "#FF5252"),
    WORKOUT_TIME("Workout Time", "mins", "#00E676"),
    WATER("Hydration", "ml", "#2979FF"),
    WEIGHT("Target Weight", "kg", "#FFAB00"),
    CUSTOM("Custom Habit", "times", "#B388FF")
}

enum class GoalPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MILESTONE("Milestone")
}

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: GoalCategory,
    val targetValue: Float,
    val unit: String,
    val period: GoalPeriod = GoalPeriod.DAILY,
    val colorHex: String = category.defaultColorHex,
    val startingValue: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
