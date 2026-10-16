package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.GoalCategory
import com.example.ui.components.BarProgressChart
import com.example.ui.components.LineTrendChart
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.MetricCaloriesColor
import com.example.ui.theme.MetricStepsColor
import com.example.ui.theme.MetricWaterColor
import com.example.ui.theme.MetricWeightColor
import com.example.ui.theme.MetricWorkoutColor
import com.example.ui.theme.PulseAmber
import com.example.ui.viewmodel.ChartDayPoint
import com.example.ui.viewmodel.ChartTimeframe
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun ChartsScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedChartCategory.collectAsStateWithLifecycle()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
    val chartDataPair by viewModel.chartData.collectAsStateWithLifecycle()
    val points = chartDataPair.first
    val stats = chartDataPair.second

    var isLineChartMode by remember { mutableStateOf(selectedCategory == GoalCategory.WEIGHT) }

    val categoryColor = when (selectedCategory) {
        GoalCategory.STEPS -> MetricStepsColor
        GoalCategory.CALORIES -> MetricCaloriesColor
        GoalCategory.WORKOUT_TIME -> MetricWorkoutColor
        GoalCategory.WATER -> MetricWaterColor
        GoalCategory.WEIGHT -> MetricWeightColor
        GoalCategory.CUSTOM -> PulseAmber
    }

    val unit = selectedCategory.defaultUnit

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("charts_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Goal Analytics",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Progress Charts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Category Filter Chips Carousel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoalCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setChartCategory(cat)
                            if (cat == GoalCategory.WEIGHT) {
                                isLineChartMode = true
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(cat.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = categoryColor.copy(alpha = 0.22f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLeadingIconColor = categoryColor
                        ),
                        modifier = Modifier.testTag("chart_category_${cat.name.lowercase()}")
                    )
                }
            }
        }

        // Timeframe Selector & Chart Type Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Timeframe Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChartTimeframe.values().forEach { tf ->
                        val isSelected = selectedTimeframe == tf
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setChartTimeframe(tf) },
                            label = { Text(tf.label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("timeframe_${tf.days}_days")
                        )
                    }
                }

                // Chart Style Toggle (Bar vs Line)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IconButton(
                            onClick = { isLineChartMode = false },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isLineChartMode) categoryColor.copy(alpha = 0.25f) else Color.Transparent)
                                .testTag("toggle_bar_chart")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bar Chart",
                                tint = if (!isLineChartMode) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { isLineChartMode = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLineChartMode) categoryColor.copy(alpha = 0.25f) else Color.Transparent)
                                .testTag("toggle_line_chart")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Line Trend",
                                tint = if (isLineChartMode) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Chart Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isLineChartMode) {
                        LineTrendChart(
                            points = points,
                            unit = unit,
                            primaryColor = categoryColor
                        )
                    } else {
                        BarProgressChart(
                            points = points,
                            unit = unit,
                            primaryColor = categoryColor
                        )
                    }
                }
            }
        }

        // Analytics Summary Cards Grid
        item {
            Text(
                text = "Performance Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Daily Average",
                    value = if (stats.average >= 1000f && unit == "steps") {
                        String.format("%,d", stats.average.toInt())
                    } else {
                        String.format("%.1f", stats.average).replace(".0", "")
                    },
                    unit = unit,
                    icon = Icons.Default.Speed,
                    accentColor = categoryColor,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Goal Hit Rate",
                    value = "${stats.goalHitRatePercent}%",
                    unit = "(${stats.daysHit}/${stats.totalDays} days)",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = if (stats.goalHitRatePercent >= 70) MetricWorkoutColor else PulseAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Peak Day",
                    value = if (stats.peakValue >= 1000f && unit == "steps") {
                        String.format("%,d", stats.peakValue.toInt())
                    } else {
                        String.format("%.1f", stats.peakValue).replace(".0", "")
                    },
                    unit = stats.peakDate,
                    icon = Icons.Default.Timeline,
                    accentColor = PulseAmber,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = if (selectedCategory == GoalCategory.WEIGHT) "Current" else "Total Period",
                    value = if (selectedCategory == GoalCategory.WEIGHT) {
                        String.format("%.1f", points.lastOrNull()?.value ?: 0f)
                    } else if (stats.total >= 1000f && unit == "steps") {
                        String.format("%,d", stats.total.toInt())
                    } else {
                        String.format("%.0f", stats.total)
                    },
                    unit = unit,
                    icon = Icons.Default.CheckCircle,
                    accentColor = categoryColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Daily Detailed History for Selected Period
        item {
            Text(
                text = "Day-by-Day Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(points.reversed()) { point ->
            DailyPointRow(point = point, unit = unit, color = categoryColor)
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyPointRow(
    point: ChartDayPoint,
    unit: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = point.fullDateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (point.isGoalMet) "Target achieved" else "Target: ${point.targetValue.toInt()} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (point.isGoalMet) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (point.value >= 1000f && unit == "steps") {
                        String.format("%,d", point.value.toInt())
                    } else {
                        String.format("%.1f", point.value).replace(".0", "")
                    } + " $unit",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (point.isGoalMet) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            }
        }
    }
}
