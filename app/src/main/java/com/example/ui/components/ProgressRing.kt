package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MetricCaloriesColor
import com.example.ui.theme.MetricStepsColor
import com.example.ui.theme.MetricWorkoutColor

@Composable
fun SingleProgressRing(
    progress: Float, // 0.0 to 1.0 (or >1.0)
    color: Color,
    backgroundColor: Color = color.copy(alpha = 0.2f),
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1.5f),
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background Track
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Progress
            val sweep = animatedProgress.value * 360f
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweep.coerceAtMost(360f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // If over 100%, draw outer overlap with accent
                if (animatedProgress.value > 1.0f) {
                    val overSweep = (animatedProgress.value - 1.0f) * 360f
                    drawArc(
                        color = Color.White.copy(alpha = 0.8f),
                        startAngle = -90f,
                        sweepAngle = overSweep.coerceAtMost(360f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx * 0.4f, cap = StrokeCap.Round)
                    )
                }
            }
        }
        content?.invoke()
    }
}

@Composable
fun MultiActivityRings(
    stepsProgress: Float,
    workoutProgress: Float,
    caloriesProgress: Float,
    size: Dp = 180.dp,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    val animSteps = remember { Animatable(0f) }
    val animWorkout = remember { Animatable(0f) }
    val animCalories = remember { Animatable(0f) }

    LaunchedEffect(stepsProgress, workoutProgress, caloriesProgress) {
        animSteps.animateTo(stepsProgress.coerceIn(0f, 1.25f), tween(900, 0, FastOutSlowInEasing))
    }
    LaunchedEffect(workoutProgress) {
        animWorkout.animateTo(workoutProgress.coerceIn(0f, 1.25f), tween(900, 150, FastOutSlowInEasing))
    }
    LaunchedEffect(caloriesProgress) {
        animCalories.animateTo(caloriesProgress.coerceIn(0f, 1.25f), tween(900, 300, FastOutSlowInEasing))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val ringStroke = 12.dp.toPx()
            val ringSpacing = 4.dp.toPx()

            // Outer Ring - Steps (Cyan)
            val outerRadius = (this.size.width - ringStroke) / 2
            val outerSize = Size(outerRadius * 2, outerRadius * 2)
            val outerTopLeft = Offset((this.size.width - outerSize.width) / 2, (this.size.height - outerSize.height) / 2)

            // Middle Ring - Calories (Coral)
            val midRadius = outerRadius - ringStroke - ringSpacing
            val midSize = Size(midRadius * 2, midRadius * 2)
            val midTopLeft = Offset((this.size.width - midSize.width) / 2, (this.size.height - midSize.height) / 2)

            // Inner Ring - Workout (Emerald)
            val innerRadius = midRadius - ringStroke - ringSpacing
            val innerSize = Size(innerRadius * 2, innerRadius * 2)
            val innerTopLeft = Offset((this.size.width - innerSize.width) / 2, (this.size.height - innerSize.height) / 2)

            // Background tracks
            drawArc(
                color = MetricStepsColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = MetricCaloriesColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = midTopLeft,
                size = midSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = MetricWorkoutColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            // Active progress arcs
            if (animSteps.value > 0f) {
                drawArc(
                    color = MetricStepsColor,
                    startAngle = -90f,
                    sweepAngle = (animSteps.value * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = outerTopLeft,
                    size = outerSize,
                    style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                )
            }
            if (animCalories.value > 0f) {
                drawArc(
                    color = MetricCaloriesColor,
                    startAngle = -90f,
                    sweepAngle = (animCalories.value * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = midTopLeft,
                    size = midSize,
                    style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                )
            }
            if (animWorkout.value > 0f) {
                drawArc(
                    color = MetricWorkoutColor,
                    startAngle = -90f,
                    sweepAngle = (animWorkout.value * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerSize,
                    style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                )
            }
        }
        content?.invoke()
    }
}
