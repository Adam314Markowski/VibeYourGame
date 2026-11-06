package com.byd.workout.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byd.workout.R
import com.byd.workout.ui.components.BydSnackbar
import com.byd.workout.ui.components.LocalBydSnackbar
import com.byd.workout.ui.exercise.ExerciseScreen
import com.byd.workout.ui.history.HistoryScreen
import com.byd.workout.ui.music.MusicScreen
import com.byd.workout.ui.plan.PlanEditScreen
import com.byd.workout.ui.plan.PlanScreen
import com.byd.workout.ui.theme.BydShapes
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType

object Routes {
    const val EXERCISE = "exercise"
    const val MUSIC = "music"
    const val PLAN = "plan"
    const val HISTORY = "history"
    const val PLAN_EDIT = "plan/edit/{dayIndex}"
    const val ARG_DAY_INDEX = "dayIndex"

    fun planEdit(dayIndex: Int) = "plan/edit/$dayIndex"
}

private enum class TopLevel(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    Exercise(Routes.EXERCISE, R.string.tab_exercise, Icons.Outlined.FitnessCenter),
    Music(Routes.MUSIC, R.string.tab_music, Icons.Outlined.MusicNote),
    Plan(Routes.PLAN, R.string.tab_plan, Icons.Outlined.CalendarMonth),
    History(Routes.HISTORY, R.string.tab_history, Icons.AutoMirrored.Outlined.FormatListBulleted),
}

@Composable
fun BydNavHost(
    navController: NavHostController = rememberNavController(),
    miniPlayer: @Composable () -> Unit = {},
) {
    val colors = BydTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbar = remember(snackbarHostState, scope) { BydSnackbar(snackbarHostState, scope) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    CompositionLocalProvider(LocalBydSnackbar provides snackbar) {
        Scaffold(
            containerColor = colors.bg,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = colors.ink,
                        contentColor = colors.bg,
                        shape = BydShapes.button,
                    )
                }
            },
            bottomBar = {
                Column(Modifier.fillMaxWidth().background(colors.surface)) {
                    miniPlayer()
                    HorizontalDivider(thickness = 1.dp, color = colors.line)
                    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                        TopLevel.entries.forEach { tab ->
                            val selected = destination?.hierarchy?.any { it.route?.substringBefore('/') == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navController.navigateToTab(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(stringResource(tab.label), style = BydType.navLabel) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.accent,
                                    selectedTextColor = colors.accent,
                                    indicatorColor = colors.accentSoft,
                                    unselectedIconColor = colors.muted,
                                    unselectedTextColor = colors.muted,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.EXERCISE,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                composable(Routes.EXERCISE) { ExerciseScreen() }
                composable(Routes.MUSIC) { MusicScreen() }
                composable(Routes.PLAN) {
                    PlanScreen(onEditDay = { day -> navController.navigate(Routes.planEdit(day)) })
                }
                composable(
                    Routes.PLAN_EDIT,
                    arguments = listOf(navArgument(Routes.ARG_DAY_INDEX) { type = NavType.IntType }),
                ) { entry ->
                    PlanEditScreen(
                        dayIndex = entry.arguments?.getInt(Routes.ARG_DAY_INDEX) ?: 0,
                        onClose = { navController.popBackStack() },
                    )
                }
                composable(Routes.HISTORY) { HistoryScreen() }
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    if (currentDestination?.route == Routes.PLAN_EDIT) popBackStack()
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
