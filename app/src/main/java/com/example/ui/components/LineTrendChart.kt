package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChartDayPoint
import kotlin.math.max
import kotlin.math.min

@Composable
fun LineTrendChart(
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
                text = "No trend data recorded yet",
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
        animProgress.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    val selectedPoint = points.getOrNull(selectedIndex) ?: points.last()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("line_trend_chart")
    ) {
        // Scrubber / Value Header
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
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
                            text = String.format("%.1f", selectedPoint.value),
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

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = primaryColor.copy(alpha = 0.18f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Target: ${selectedPoint.targetValue} $unit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // Canvas Spline Chart
        val allValues = points.map { it.value } + listOf(selectedPoint.targetValue)
        val minVal = (allValues.minOrNull() ?: 50f) * 0.95f
        val maxVal = (allValues.maxOrNull() ?: 100f) * 1.05f
        val range = max(maxVal - minVal, 1f)

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
                            val stepX = size.width / (n - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, n - 1)
                            selectedIndex = index
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            val n = points.size
                            val stepX = size.width / (n - 1).coerceAtLeast(1)
                            val index = (change.position.x / stepX).toInt().coerceIn(0, n - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val bottomLabelHeight = 24.dp.toPx()
                val graphHeight = canvasHeight - bottomLabelHeight

                val numPoints = points.size
                val stepX = canvasWidth / (numPoints - 1).coerceAtLeast(1)

                // Benchmark Target Line
                val targetRatio = ((selectedPoint.targetValue - minVal) / range).coerceIn(0f, 1f)
                val targetY = graphHeight - (targetRatio * graphHeight)

                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(0f, targetY),
                    end = Offset(canvasWidth, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )

                if (numPoints >= 2) {
                    val coords = points.mapIndexed { index, point ->
                        val x = index * stepX
                        val ratio = ((point.value - minVal) / range).coerceIn(0f, 1f)
                        val y = graphHeight - (ratio * graphHeight * animProgress.value)
                        Offset(x, y)
                    }

                    // Build smooth bezier path
                    val strokePath = Path()
                    val fillPath = Path()

                    coords.forEachIndexed { i, pt ->
                        if (i == 0) {
                            strokePath.moveTo(pt.x, pt.y)
                            fillPath.moveTo(pt.x, graphHeight)
                            fillPath.lineTo(pt.x, pt.y)
                        } else {
                            val prev = coords[i - 1]
                            val cx1 = prev.x + (pt.x - prev.x) / 2f
                            val cy1 = prev.y
                            val cx2 = prev.x + (pt.x - prev.x) / 2f
                            val cy2 = pt.y
                            strokePath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                            fillPath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                        }
                    }

                    fillPath.lineTo(coords.last().x, graphHeight)
                    fillPath.close()

                    // Draw gradient area under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.03f)
                            ),
                            startY = 0f,
                            endY = graphHeight
                        )
                    )

                    // Draw smooth curve stroke
                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw points & scrubber highlight
                    coords.forEachIndexed { i, pt ->
                        val isSelected = i == selectedIndex
                        if (isSelected) {
                            // Vertical guide line for selected point
                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = Offset(pt.x, 0f),
                                end = Offset(pt.x, graphHeight),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )

                            // Outer glow circle
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.3f),
                                radius = 12.dp.toPx(),
                                center = pt
                            )
                            // Solid center circle
                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                        } else if (numPoints <= 14) {
                            drawCircle(
                                color = primaryColor,
                                radius = 3.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            // X-Axis Date Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val labelIndices = if (points.size <= 7) {
                    points.indices.toList()
                } else {
                    listOf(0, points.size / 2, points.lastIndex)
                }

                labelIndices.forEach { i ->
                    val pt = points[i]
                    Text(
                        text = pt.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (i == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (i == selectedIndex) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
