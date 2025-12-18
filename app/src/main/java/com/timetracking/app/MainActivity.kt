package com.timetracking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.timetracking.app.navigation.BottomNavigationBar
import com.timetracking.app.navigation.NavGraph
import com.timetracking.app.ui.theme.TimeTrackingAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeTrackingAppTheme {
                val navController = rememberNavController()
                
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { paddingValues ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        NavGraph(
                            navController = navController,
                            startDestination = com.timetracking.app.navigation.Screen.Home.route
                        )
                    }
                }
            }
        }
    }
}
