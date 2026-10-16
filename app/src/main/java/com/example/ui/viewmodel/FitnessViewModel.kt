package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ActivityLogEntity
import com.example.data.entity.GoalCategory
import com.example.data.entity.GoalEntity
import com.example.data.entity.GoalPeriod
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ChartTimeframe(val days: Int, val label: String) {
    SEVEN_DAYS(7, "7 Days"),
    FOURTEEN_DAYS(14, "14 Days"),
    THIRTY_DAYS(30, "30 Days")
}

data class ChartDayPoint(
    val dayTimestamp: Long,
    val dayLabel: String,
    val fullDateLabel: String,
    val value: Float,
    val targetValue: Float,
    val isGoalMet: Boolean,
    val diffFromTarget: Float
)

data class ChartSummaryStats(
    val average: Float,
    val total: Float,
    val peakValue: Float,
    val peakDate: String,
    val goalHitRatePercent: Int,
    val daysHit: Int,
    val totalDays: Int
)

data class TodayProgress(
    val steps: Float = 0f,
    val stepsTarget: Float = 10000f,
    val calories: Float = 0f,
    val caloriesTarget: Float = 500f,
    val workoutMins: Float = 0f,
    val workoutTarget: Float = 45f,
    val waterMl: Float = 0f,
    val waterTarget: Float = 2500f,
    val currentWeight: Float = 0f,
    val targetWeight: Float = 70f
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = FitnessRepository(database.goalDao(), database.activityLogDao())
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActivityLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected state for charts
    private val _selectedChartCategory = MutableStateFlow(GoalCategory.STEPS)
    val selectedChartCategory: StateFlow<GoalCategory> = _selectedChartCategory.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(ChartTimeframe.SEVEN_DAYS)
    val selectedTimeframe: StateFlow<ChartTimeframe> = _selectedTimeframe.asStateFlow()

    fun setChartCategory(category: GoalCategory) {
        _selectedChartCategory.value = category
    }

    fun setChartTimeframe(timeframe: ChartTimeframe) {
        _selectedTimeframe.value = timeframe
    }

    // Today's summary progress
    val todayProgress: StateFlow<TodayProgress> = combine(goals, allLogs) { goalList, logList ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        val todayLogs = logList.filter { it.timestamp >= startOfToday }

        val stepsTarget = goalList.firstOrNull { it.category == GoalCategory.STEPS }?.targetValue ?: 10000f
        val caloriesTarget = goalList.firstOrNull { it.category == GoalCategory.CALORIES }?.targetValue ?: 500f
        val workoutTarget = goalList.firstOrNull { it.category == GoalCategory.WORKOUT_TIME }?.targetValue ?: 45f
        val waterTarget = goalList.firstOrNull { it.category == GoalCategory.WATER }?.targetValue ?: 2500f
        val targetWeight = goalList.firstOrNull { it.category == GoalCategory.WEIGHT }?.targetValue ?: 70f

        val todaySteps = todayLogs.filter { it.category == GoalCategory.STEPS }.sumOf { it.value.toDouble() }.toFloat()
        val todayCalories = todayLogs.filter { it.category == GoalCategory.CALORIES }.sumOf { it.value.toDouble() }.toFloat()
        val todayWorkout = todayLogs.filter { it.category == GoalCategory.WORKOUT_TIME }.sumOf { it.value.toDouble() }.toFloat()
        val todayWater = todayLogs.filter { it.category == GoalCategory.WATER }.sumOf { it.value.toDouble() }.toFloat()

        val latestWeightLog = logList.filter { it.category == GoalCategory.WEIGHT }.maxByOrNull { it.timestamp }
        val currentWeight = latestWeightLog?.value ?: 73.4f

        TodayProgress(
            steps = todaySteps,
            stepsTarget = stepsTarget,
            calories = todayCalories,
            caloriesTarget = caloriesTarget,
            workoutMins = todayWorkout,
            workoutTarget = workoutTarget,
            waterMl = todayWater,
            waterTarget = waterTarget,
            currentWeight = currentWeight,
            targetWeight = targetWeight
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayProgress())

    // Active Day Streak calculation
    val streakDays: StateFlow<Int> = combine(goals, allLogs) { goalList, logList ->
        calculateStreak(goalList, logList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    // Chart Data calculations
    val chartData: StateFlow<Pair<List<ChartDayPoint>, ChartSummaryStats>> = combine(
        goals,
        allLogs,
        _selectedChartCategory,
        _selectedTimeframe
    ) { goalList, logList, category, timeframe ->
        computeChartPoints(goalList, logList, category, timeframe)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Pair(emptyList(), ChartSummaryStats(0f, 0f, 0f, "", 0, 0, 0))
    )

    private fun computeChartPoints(
        goals: List<GoalEntity>,
        logs: List<ActivityLogEntity>,
        category: GoalCategory,
        timeframe: ChartTimeframe
    ): Pair<List<ChartDayPoint>, ChartSummaryStats> {
        val targetGoal = goals.firstOrNull { it.category == category }
        val targetValue = targetGoal?.targetValue ?: when (category) {
            GoalCategory.STEPS -> 10000f
            GoalCategory.CALORIES -> 500f
            GoalCategory.WORKOUT_TIME -> 45f
            GoalCategory.WATER -> 2500f
            GoalCategory.WEIGHT -> 70f
            GoalCategory.CUSTOM -> 10f
        }

        val daysCount = timeframe.days
        val dayPoints = mutableListOf<ChartDayPoint>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateMonthFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        var totalSum = 0f
        var peakValue = 0f
        var peakDate = ""
        var daysHit = 0

        // For weight, if a day doesn't have a direct log, carry forward previous known weight
        var lastKnownWeight = 75f

        for (i in (daysCount - 1) downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis - TimeUnit.DAYS.toMillis(i.toLong())
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1) - 1

            val dayLogs = logs.filter {
                it.category == category && it.timestamp in startOfDay..endOfDay
            }

            val dayValue = if (category == GoalCategory.WEIGHT) {
                val latest = dayLogs.maxByOrNull { it.timestamp }
                if (latest != null) {
                    lastKnownWeight = latest.value
                    latest.value
                } else {
                    lastKnownWeight
                }
            } else {
                dayLogs.sumOf { it.value.toDouble() }.toFloat()
            }

            val isGoalMet = if (category == GoalCategory.WEIGHT) {
                dayValue <= targetValue
            } else {
                dayValue >= targetValue
            }

            if (isGoalMet) daysHit++
            totalSum += dayValue

            if (dayValue > peakValue) {
                peakValue = dayValue
                peakDate = dateMonthFormat.format(dayCal.time)
            }

            val label = if (daysCount <= 7) dayFormat.format(dayCal.time) else dateMonthFormat.format(dayCal.time)
            val fullLabel = fullDateFormat.format(dayCal.time)

            dayPoints.add(
                ChartDayPoint(
                    dayTimestamp = startOfDay,
                    dayLabel = label,
                    fullDateLabel = fullLabel,
                    value = dayValue,
                    targetValue = targetValue,
                    isGoalMet = isGoalMet,
                    diffFromTarget = dayValue - targetValue
                )
            )
        }

        val average = if (daysCount > 0) totalSum / daysCount else 0f
        val hitRate = if (daysCount > 0) ((daysHit.toFloat() / daysCount) * 100).toInt() else 0

        val stats = ChartSummaryStats(
            average = average,
            total = totalSum,
            peakValue = peakValue,
            peakDate = peakDate,
            goalHitRatePercent = hitRate,
            daysHit = daysHit,
            totalDays = daysCount
        )

        return Pair(dayPoints, stats)
    }

    private fun calculateStreak(goals: List<GoalEntity>, logs: List<ActivityLogEntity>): Int {
        val stepsGoal = goals.firstOrNull { it.category == GoalCategory.STEPS }?.targetValue ?: 8000f
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        var streak = 0
        // Check back up to 30 days
        for (i in 0 until 30) {
            val startOfDay = cal.timeInMillis - TimeUnit.DAYS.toMillis(i.toLong())
            val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1) - 1

            val dayLogs = logs.filter { it.timestamp in startOfDay..endOfDay }
            val daySteps = dayLogs.filter { it.category == GoalCategory.STEPS }.sumOf { it.value.toDouble() }.toFloat()
            val dayWorkout = dayLogs.filter { it.category == GoalCategory.WORKOUT_TIME }.sumOf { it.value.toDouble() }.toFloat()

            // If day met either 70% of step goal or at least 20 mins workout
            if (daySteps >= (stepsGoal * 0.7f) || dayWorkout >= 20f || i == 0) {
                streak++
            } else {
                break
            }
        }
        return if (streak == 0) 1 else streak
    }

    // Quick Actions
    fun logQuickSteps(steps: Float) {
        viewModelScope.launch {
            repository.insertLog(
                ActivityLogEntity(
                    category = GoalCategory.STEPS,
                    activityName = "Pedometer Sync",
                    value = steps,
                    unit = "steps",
                    notes = "Quick step entry"
                )
            )
        }
    }

    fun logQuickWater(ml: Float) {
        viewModelScope.launch {
            repository.insertLog(
                ActivityLogEntity(
                    category = GoalCategory.WATER,
                    activityName = "Hydration Glass",
                    value = ml,
                    unit = "ml",
                    notes = "Quick water intake"
                )
            )
        }
    }

    fun logQuickWorkout(mins: Float, workoutType: String) {
        viewModelScope.launch {
            repository.insertLog(
                ActivityLogEntity(
                    category = GoalCategory.WORKOUT_TIME,
                    activityName = workoutType,
                    value = mins,
                    unit = "mins",
                    notes = "Quick workout session"
                )
            )
        }
    }

    fun logQuickCalories(kcal: Float) {
        viewModelScope.launch {
            repository.insertLog(
                ActivityLogEntity(
                    category = GoalCategory.CALORIES,
                    activityName = "Burned Calories",
                    value = kcal,
                    unit = "kcal",
                    notes = "Active calorie burn"
                )
            )
        }
    }

    fun logActivity(
        category: GoalCategory,
        activityName: String,
        value: Float,
        unit: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertLog(
                ActivityLogEntity(
                    category = category,
                    activityName = activityName,
                    value = value,
                    unit = unit,
                    notes = notes
                )
            )
        }
    }

    fun deleteLog(log: ActivityLogEntity) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun addGoal(
        title: String,
        category: GoalCategory,
        targetValue: Float,
        unit: String,
        period: GoalPeriod,
        colorHex: String,
        startingValue: Float = 0f
    ) {
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    title = title,
                    category = category,
                    targetValue = targetValue,
                    unit = unit,
                    period = period,
                    colorHex = colorHex,
                    startingValue = startingValue
                )
            )
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }
}
