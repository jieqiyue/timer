package com.timetracking.app.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Statistics : Screen("statistics")
    object Timer : Screen("timer/{activityId}") {
        fun createRoute(activityId: Long) = "timer/$activityId"
    }
}
