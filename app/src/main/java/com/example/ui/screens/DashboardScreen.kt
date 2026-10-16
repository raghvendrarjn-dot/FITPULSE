package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ActivityLogEntity
import com.example.data.entity.GoalCategory
import com.example.ui.components.DashboardMetricCard
import com.example.ui.components.MultiActivityRings
import com.example.ui.components.StreakBanner
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.MetricCaloriesColor
import com.example.ui.theme.MetricStepsColor
import com.example.ui.theme.MetricWaterColor
import com.example.ui.theme.MetricWorkoutColor
import com.example.ui.viewmodel.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FitnessViewModel,
    onNavigateToCharts: () -> Unit,
    onOpenLogDialog: (GoalCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayProgress by viewModel.todayProgress.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val recentLogs by viewModel.allLogs.collectAsStateWithLifecycle()

    val todayDateStr = rememberDateString()

    val stepsProgress = if (todayProgress.stepsTarget > 0) todayProgress.steps / todayProgress.stepsTarget else 0f
    val workoutProgress = if (todayProgress.workoutTarget > 0) todayProgress.workoutMins / todayProgress.workoutTarget else 0f
    val caloriesProgress = if (todayProgress.caloriesTarget > 0) todayProgress.calories / todayProgress.caloriesTarget else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayDateStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Today\'s Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                ElevatedButton(
                    onClick = onNavigateToCharts,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("view_charts_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "View Charts",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Charts", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Multi-Activity Rings Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_rings_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Fitness Rings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Overall ${(stepsProgress.coerceAtMost(1f) * 0.4f + caloriesProgress.coerceAtMost(1f) * 0.35f + workoutProgress.coerceAtMost(1f) * 0.25f).let { (it * 100).toInt() }}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MultiActivityRings(
                            stepsProgress = stepsProgress,
                            workoutProgress = workoutProgress,
                            caloriesProgress = caloriesProgress,
                            size = 150.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "${todayProgress.workoutMins.toInt()}m",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            RingLegendItem(
                                color = MetricStepsColor,
                                label = "Move Steps",
                                value = "${String.format("%,d", todayProgress.steps.toInt())} / ${todayProgress.stepsTarget.toInt()}",
                                percent = (stepsProgress * 100).toInt()
                            )
                            RingLegendItem(
                                color = MetricCaloriesColor,
                                label = "Calories",
                                value = "${todayProgress.calories.toInt()} / ${todayProgress.caloriesTarget.toInt()} kcal",
                                percent = (caloriesProgress * 100).toInt()
                            )
                            RingLegendItem(
                                color = MetricWorkoutColor,
                                label = "Exercise",
                                value = "${todayProgress.workoutMins.toInt()} / ${todayProgress.workoutTarget.toInt()} mins",
                                percent = (workoutProgress * 100).toInt()
                            )
                        }
                    }
                }
            }
        }

        // Streak Banner
        item {
            StreakBanner(streakDays = streakDays)
        }

        // Quick Log Action Chips
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.logQuickSteps(1000f) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_steps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = MetricStepsColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1k", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.logQuickWater(250f) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_water_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MetricWaterColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+250ml", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.logQuickCalories(150f) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_cal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MetricCaloriesColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+150 kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onOpenLogDialog(GoalCategory.WORKOUT_TIME) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_workout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MetricWorkoutColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Metric Cards for each goal
        item {
            Text(
                text = "Target Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            DashboardMetricCard(
                title = "Daily Step Count",
                currentValue = todayProgress.steps,
                targetValue = todayProgress.stepsTarget,
                unit = "steps",
                category = GoalCategory.STEPS,
                color = MetricStepsColor,
                onQuickAdd = { onOpenLogDialog(GoalCategory.STEPS) }
            )
        }

        item {
            DashboardMetricCard(
                title = "Workout Duration",
                currentValue = todayProgress.workoutMins,
                targetValue = todayProgress.workoutTarget,
                unit = "mins",
                category = GoalCategory.WORKOUT_TIME,
                color = MetricWorkoutColor,
                onQuickAdd = { onOpenLogDialog(GoalCategory.WORKOUT_TIME) }
            )
        }

        item {
            DashboardMetricCard(
                title = "Active Calories",
                currentValue = todayProgress.calories,
                targetValue = todayProgress.caloriesTarget,
                unit = "kcal",
                category = GoalCategory.CALORIES,
                color = MetricCaloriesColor,
                onQuickAdd = { onOpenLogDialog(GoalCategory.CALORIES) }
            )
        }

        item {
            DashboardMetricCard(
                title = "Daily Hydration",
                currentValue = todayProgress.waterMl,
                targetValue = todayProgress.waterTarget,
                unit = "ml",
                category = GoalCategory.WATER,
                color = MetricWaterColor,
                onQuickAdd = { onOpenLogDialog(GoalCategory.WATER) }
            )
        }

        // Recent Logs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val topLogs = recentLogs.take(5)
        if (topLogs.isEmpty()) {
            item {
                Text(
                    text = "No activities logged yet. Tap + to log!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(topLogs) { log ->
                RecentLogMiniItem(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun RingLegendItem(
    color: Color,
    label: String,
    value: String,
    percent: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentLogMiniItem(log: ActivityLogEntity) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(log.timestamp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(log.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = log.activityName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "+${if (log.value >= 1000f && log.unit == "steps") String.format("%,d", log.value.toInt()) else String.format("%.1f", log.value).replace(".0", "")} ${log.unit}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun rememberDateString(): String {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    return dateFormat.format(Date())
}
