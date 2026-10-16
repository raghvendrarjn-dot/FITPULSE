package com.example.data.repository

import com.example.data.dao.ActivityLogDao
import com.example.data.dao.GoalDao
import com.example.data.entity.ActivityLogEntity
import com.example.data.entity.GoalCategory
import com.example.data.entity.GoalEntity
import com.example.data.entity.GoalPeriod
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FitnessRepository(
    private val goalDao: GoalDao,
    private val logDao: ActivityLogDao
) {
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val allLogs: Flow<List<ActivityLogEntity>> = logDao.getAllLogs()

    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<ActivityLogEntity>> =
        logDao.getLogsBetween(startTime, endTime)

    fun getLogsByCategory(category: GoalCategory): Flow<List<ActivityLogEntity>> =
        logDao.getLogsByCategory(category)

    fun getRecentLogs(limit: Int = 20): Flow<List<ActivityLogEntity>> =
        logDao.getRecentLogs(limit)

    suspend fun insertGoal(goal: GoalEntity): Long = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = goalDao.deleteGoal(goal)
    suspend fun deleteGoalById(id: Long) = goalDao.deleteGoalById(id)

    suspend fun insertLog(log: ActivityLogEntity): Long = logDao.insertLog(log)
    suspend fun deleteLog(log: ActivityLogEntity) = logDao.deleteLog(log)
    suspend fun deleteLogById(id: Long) = logDao.deleteLogById(id)

    suspend fun seedInitialDataIfNeeded() {
        val count = goalDao.getGoalCount()
        if (count > 0) return

        val now = System.currentTimeMillis()
        val defaultGoals = listOf(
            GoalEntity(
                title = "Daily Steps Target",
                category = GoalCategory.STEPS,
                targetValue = 10000f,
                unit = "steps",
                period = GoalPeriod.DAILY,
                colorHex = "#00E5FF",
                startingValue = 0f,
                createdAt = now - TimeUnit.DAYS.toMillis(14)
            ),
            GoalEntity(
                title = "Active Workout Time",
                category = GoalCategory.WORKOUT_TIME,
                targetValue = 45f,
                unit = "mins",
                period = GoalPeriod.DAILY,
                colorHex = "#00E676",
                startingValue = 0f,
                createdAt = now - TimeUnit.DAYS.toMillis(14)
            ),
            GoalEntity(
                title = "Daily Active Calories",
                category = GoalCategory.CALORIES,
                targetValue = 500f,
                unit = "kcal",
                period = GoalPeriod.DAILY,
                colorHex = "#FF5252",
                startingValue = 0f,
                createdAt = now - TimeUnit.DAYS.toMillis(14)
            ),
            GoalEntity(
                title = "Daily Hydration",
                category = GoalCategory.WATER,
                targetValue = 2500f,
                unit = "ml",
                period = GoalPeriod.DAILY,
                colorHex = "#2979FF",
                startingValue = 0f,
                createdAt = now - TimeUnit.DAYS.toMillis(14)
            ),
            GoalEntity(
                title = "Reach Goal Weight",
                category = GoalCategory.WEIGHT,
                targetValue = 70.0f,
                unit = "kg",
                period = GoalPeriod.MILESTONE,
                colorHex = "#FFAB00",
                startingValue = 76.5f,
                createdAt = now - TimeUnit.DAYS.toMillis(14)
            )
        )
        val insertedGoalIds = goalDao.insertAll(defaultGoals)

        // Seed realistic past 14 days of logs
        val sampleLogs = mutableListOf<ActivityLogEntity>()
        val cal = Calendar.getInstance()

        // Days from 13 days ago to today (0)
        val stepSamples = listOf(8420, 10250, 9300, 11400, 7850, 12600, 10100, 8900, 10450, 11800, 9600, 10750, 13100, 8650)
        val calorieSamples = listOf(420, 560, 480, 610, 390, 680, 530, 470, 580, 640, 510, 590, 720, 460)
        val workoutSamples = listOf(35, 50, 40, 60, 30, 75, 45, 40, 55, 60, 45, 50, 65, 40)
        val waterSamples = listOf(2200, 2600, 2400, 2800, 2100, 3000, 2500, 2300, 2700, 2900, 2500, 2650, 3100, 2000)
        val weightSamples = listOf(76.2f, 75.9f, 75.8f, 75.5f, 75.4f, 75.1f, 74.9f, 74.8f, 74.5f, 74.3f, 74.1f, 73.8f, 73.6f, 73.4f)
        val workoutTypes = listOf("Morning Jog", "HIIT Session", "Cycling", "Strength Training", "Power Walk", "Full Body Circuit", "Core & Cardio")

        for (i in 13 downTo 0) {
            val dayIndex = 13 - i
            cal.timeInMillis = now - TimeUnit.DAYS.toMillis(i.toLong())
            cal.set(Calendar.HOUR_OF_DAY, 18)
            cal.set(Calendar.MINUTE, 30)
            val dayTimestamp = cal.timeInMillis

            // Steps
            val stepsVal = stepSamples[dayIndex].toFloat()
            sampleLogs.add(
                ActivityLogEntity(
                    goalId = insertedGoalIds.getOrNull(0),
                    category = GoalCategory.STEPS,
                    activityName = "Daily Step Count",
                    value = stepsVal,
                    unit = "steps",
                    timestamp = dayTimestamp,
                    notes = "Tracked daily step goal"
                )
            )

            // Calories
            val calVal = calorieSamples[dayIndex].toFloat()
            sampleLogs.add(
                ActivityLogEntity(
                    goalId = insertedGoalIds.getOrNull(2),
                    category = GoalCategory.CALORIES,
                    activityName = "Active Calories",
                    value = calVal,
                    unit = "kcal",
                    timestamp = dayTimestamp,
                    notes = "Estimated workout & active burn"
                )
            )

            // Workout
            val workoutVal = workoutSamples[dayIndex].toFloat()
            sampleLogs.add(
                ActivityLogEntity(
                    goalId = insertedGoalIds.getOrNull(1),
                    category = GoalCategory.WORKOUT_TIME,
                    activityName = workoutTypes[dayIndex % workoutTypes.size],
                    value = workoutVal,
                    unit = "mins",
                    timestamp = dayTimestamp,
                    notes = "Felt great and energized!"
                )
            )

            // Water
            val waterVal = waterSamples[dayIndex].toFloat()
            sampleLogs.add(
                ActivityLogEntity(
                    goalId = insertedGoalIds.getOrNull(3),
                    category = GoalCategory.WATER,
                    activityName = "Daily Hydration",
                    value = waterVal,
                    unit = "ml",
                    timestamp = dayTimestamp,
                    notes = "Hydration goal"
                )
            )

            // Weight (log every other day)
            if (dayIndex % 2 == 0 || i == 0) {
                val weightVal = weightSamples[dayIndex]
                sampleLogs.add(
                    ActivityLogEntity(
                        goalId = insertedGoalIds.getOrNull(4),
                        category = GoalCategory.WEIGHT,
                        activityName = "Weight Measurement",
                        value = weightVal,
                        unit = "kg",
                        timestamp = dayTimestamp,
                        notes = "Morning fasted check-in"
                    )
                )
            }
        }

        logDao.insertAll(sampleLogs)
    }
}
