package com.timetracking.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.timetracking.app.presentation.home.HomeScreen
import com.timetracking.app.presentation.timer.TimerScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            HomeScreen(
                onActivityClick = { activityId ->
                    navController.navigate(Screen.Timer.createRoute(activityId))
                }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("activityId") {
                    type = NavType.LongType
                }
            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
            TimerScreen(
                activityId = activityId,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Statistics.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            com.timetracking.app.presentation.statistics.StatisticsScreen()
        }
    }
}
