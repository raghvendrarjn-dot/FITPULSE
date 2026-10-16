package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.entity.GoalCategory
import com.example.data.entity.GoalPeriod

class Converters {
    @TypeConverter
    fun fromGoalCategory(value: GoalCategory?): String? = value?.name

    @TypeConverter
    fun toGoalCategory(value: String?): GoalCategory? = value?.let {
        try {
            GoalCategory.valueOf(it)
        } catch (e: Exception) {
            GoalCategory.CUSTOM
        }
    }

    @TypeConverter
    fun fromGoalPeriod(value: GoalPeriod?): String? = value?.name

    @TypeConverter
    fun toGoalPeriod(value: String?): GoalPeriod? = value?.let {
        try {
            GoalPeriod.valueOf(it)
        } catch (e: Exception) {
            GoalPeriod.DAILY
        }
    }
}
