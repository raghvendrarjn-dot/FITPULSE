package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.GoalCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogActivityDialog(
    initialCategory: GoalCategory = GoalCategory.WORKOUT_TIME,
    onDismiss: () -> Unit,
    onSaveLog: (category: GoalCategory, activityName: String, value: Float, unit: String, notes: String) -> Unit
) {
    var category by remember { mutableStateOf(initialCategory) }
    var activityName by remember {
        mutableStateOf(
            when (initialCategory) {
                GoalCategory.STEPS -> "Daily Steps"
                GoalCategory.CALORIES -> "Active Burn"
                GoalCategory.WORKOUT_TIME -> "Running"
                GoalCategory.WATER -> "Water Bottle"
                GoalCategory.WEIGHT -> "Weight Check-in"
                GoalCategory.CUSTOM -> "Fitness Habit"
            }
        )
    }
    var valueStr by remember {
        mutableStateOf(
            when (initialCategory) {
                GoalCategory.STEPS -> "2500"
                GoalCategory.CALORIES -> "300"
                GoalCategory.WORKOUT_TIME -> "30"
                GoalCategory.WATER -> "500"
                GoalCategory.WEIGHT -> "73.5"
                GoalCategory.CUSTOM -> "1"
            }
        )
    }
    var unit by remember { mutableStateOf(initialCategory.defaultUnit) }
    var notes by remember { mutableStateOf("") }

    val workoutPresets = listOf("Running", "HIIT Cardio", "Strength Training", "Cycling", "Yoga", "Swimming", "Walking")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("log_activity_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Log Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoalCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                unit = cat.defaultUnit
                                activityName = when (cat) {
                                    GoalCategory.STEPS -> "Daily Steps"
                                    GoalCategory.CALORIES -> "Active Burn"
                                    GoalCategory.WORKOUT_TIME -> "Running"
                                    GoalCategory.WATER -> "Water Bottle"
                                    GoalCategory.WEIGHT -> "Weight Check-in"
                                    GoalCategory.CUSTOM -> "Exercise Habit"
                                }
                                valueStr = when (cat) {
                                    GoalCategory.STEPS -> "2000"
                                    GoalCategory.CALORIES -> "250"
                                    GoalCategory.WORKOUT_TIME -> "30"
                                    GoalCategory.WATER -> "500"
                                    GoalCategory.WEIGHT -> "73.0"
                                    GoalCategory.CUSTOM -> "1"
                                }
                            },
                            label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (category == GoalCategory.WORKOUT_TIME) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Quick Workout Types",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        workoutPresets.forEach { preset ->
                            SuggestionChip(
                                onClick = { activityName = preset },
                                label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Activity Name
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Activity / Workout Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Value and Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Amount / Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("activity_value_input")
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("activity_unit_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Felt energetic, 5km outdoor run") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_notes_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_log_button")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val valFloat = valueStr.toFloatOrNull() ?: 0f
                            if (activityName.isNotBlank() && valFloat > 0f) {
                                onSaveLog(category, activityName.trim(), valFloat, unit.trim(), notes.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("save_log_button")
                    ) {
                        Text("Save Log")
                    }
                }
            }
        }
    }
}
