package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChartDayPoint
import kotlin.math.max

@Composable
fun BarProgressChart(
    points: List<ChartDayPoint>,
    unit: String,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No activity recorded for this period yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableIntStateOf(points.lastIndex) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        if (selectedIndex !in points.indices) {
            selectedIndex = points.lastIndex
        }
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
    }

    val selectedPoint = points.getOrNull(selectedIndex) ?: points.last()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bar_progress_chart")
    ) {
        // Interactive Inspection Tooltip / Header
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("chart_tooltip_card")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = selectedPoint.fullDateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (selectedPoint.value >= 1000f && unit == "steps") {
                                    String.format("%,d", selectedPoint.value.toInt())
                                } else {
                                    String.format("%.1f", selectedPoint.value).replace(".0", "")
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // Goal Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedPoint.isGoalMet) {
                            primaryColor.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedPoint.isGoalMet) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (selectedPoint.isGoalMet) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedPoint.isGoalMet) {
                                    "Target Met! 🎉"
                                } else {
                                    val diff = selectedPoint.targetValue - selectedPoint.value
                                    "-${if (diff >= 1000f) String.format("%,d", diff.toInt()) else String.format("%.0f", diff)} $unit"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedPoint.isGoalMet) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Benchmark Target Legend & info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(primaryColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Achieved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Target Goal (${selectedPoint.targetValue.toInt()} $unit)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // The Canvas Bar Chart
        val targetVal = selectedPoint.targetValue
        val maxDataVal = points.maxOfOrNull { it.value } ?: targetVal
        val chartMax = max(maxDataVal, targetVal) * 1.25f

        val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        val textLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        val barTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val n = points.size
                            val barSlotWidth = size.width / n
                            val index = (offset.x / barSlotWidth).toInt().coerceIn(0, n - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val bottomLabelHeight = 24.dp.toPx()
                val graphHeight = canvasHeight - bottomLabelHeight

                val numPoints = points.size
                val slotWidth = canvasWidth / numPoints
                val barWidth = (slotWidth * 0.58f).coerceIn(8.dp.toPx(), 36.dp.toPx())

                // Draw horizontal target benchmark dashed line
                val targetRatio = (targetVal / chartMax).coerceIn(0f, 1f)
                val targetY = graphHeight - (targetRatio * graphHeight)

                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(0f, targetY),
                    end = Offset(canvasWidth, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )

                // Draw base grid line
                drawLine(
                    color = axisColor,
                    start = Offset(0f, graphHeight),
                    end = Offset(canvasWidth, graphHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw Bars
                points.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    val slotCenterX = (index * slotWidth) + (slotWidth / 2f)
                    val barLeft = slotCenterX - (barWidth / 2f)

                    val valueRatio = ((point.value / chartMax) * animProgress.value).coerceIn(0f, 1f)
                    val barHeight = (valueRatio * graphHeight).coerceAtLeast(4.dp.toPx())
                    val barTop = graphHeight - barHeight

                    // Bar Color
                    val barColor = when {
                        isSelected && point.isGoalMet -> primaryColor
                        isSelected && !point.isGoalMet -> primaryColor.copy(alpha = 0.85f)
                        point.isGoalMet -> primaryColor.copy(alpha = 0.75f)
                        else -> primaryColor.copy(alpha = 0.35f)
                    }

                    // Background track for the bar
                    drawRoundRect(
                        color = barTrackColor,
                        topLeft = Offset(barLeft, 0f),
                        size = Size(barWidth, graphHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Active filled bar
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw selection glow / indicator ring around bar top if selected
                    if (isSelected) {
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = Offset(slotCenterX, barTop + 4.dp.toPx())
                        )
                    }
                }
            }

            // X-Axis labels underneath the canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                points.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = point.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) primaryColor else textLabelColor
                    )
                }
            }
        }
    }
}
