package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.entity.GoalCategory
import com.example.ui.dialogs.AddGoalDialog
import com.example.ui.dialogs.LogActivityDialog
import com.example.ui.screens.ChartsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FitnessViewModel

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_CHARTS = "charts"
const val ROUTE_GOALS = "goals"
const val ROUTE_HISTORY = "history"

sealed class NavItem(val route: String, val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : NavItem(ROUTE_DASHBOARD, "Today", Icons.Default.Dashboard, "nav_dashboard")
    object Charts : NavItem(ROUTE_CHARTS, "Charts", Icons.Default.BarChart, "nav_charts")
    object Goals : NavItem(ROUTE_GOALS, "Goals", Icons.Default.Flag, "nav_goals")
    object History : NavItem(ROUTE_HISTORY, "History", Icons.Default.History, "nav_history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FitPulseApp()
            }
        }
    }
}

@Composable
fun FitPulseApp(viewModel: FitnessViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    var initialLogCategory by remember { mutableStateOf(GoalCategory.WORKOUT_TIME) }

    val navItems = listOf(
        NavItem.Dashboard,
        NavItem.Charts,
        NavItem.Goals,
        NavItem.History
    )

    // Back handling: return to dashboard if on another tab
    if (currentRoute != ROUTE_DASHBOARD) {
        BackHandler {
            navController.navigate(ROUTE_DASHBOARD) {
                popUpTo(ROUTE_DASHBOARD) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(ROUTE_DASHBOARD) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == ROUTE_DASHBOARD || currentRoute == ROUTE_HISTORY) {
                FloatingActionButton(
                    onClick = {
                        initialLogCategory = GoalCategory.WORKOUT_TIME
                        showLogDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_log_activity")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Activity"
                    )
                }
            } else if (currentRoute == ROUTE_GOALS) {
                FloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_new_goal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Goal"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCharts = {
                        navController.navigate(ROUTE_CHARTS) {
                            launchSingleTop = true
                        }
                    },
                    onOpenLogDialog = { category ->
                        initialLogCategory = category
                        showLogDialog = true
                    }
                )
            }

            composable(ROUTE_CHARTS) {
                ChartsScreen(viewModel = viewModel)
            }

            composable(ROUTE_GOALS) {
                GoalsScreen(
                    viewModel = viewModel,
                    onOpenAddGoal = { showAddGoalDialog = true }
                )
            }

            composable(ROUTE_HISTORY) {
                HistoryScreen(
                    viewModel = viewModel,
                    onOpenLogDialog = { category ->
                        initialLogCategory = category
                        showLogDialog = true
                    }
                )
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSaveGoal = { title, category, targetValue, unit, period, colorHex ->
                viewModel.addGoal(
                    title = title,
                    category = category,
                    targetValue = targetValue,
                    unit = unit,
                    period = period,
                    colorHex = colorHex
                )
                showAddGoalDialog = false
            }
        )
    }

    // Log Activity Dialog
    if (showLogDialog) {
        LogActivityDialog(
            initialCategory = initialLogCategory,
            onDismiss = { showLogDialog = false },
            onSaveLog = { category, activityName, value, unit, notes ->
                viewModel.logActivity(
                    category = category,
                    activityName = activityName,
                    value = value,
                    unit = unit,
                    notes = notes
                )
                showLogDialog = false
            }
        )
    }
}
