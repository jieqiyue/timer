package com.timetracking.app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes for responsive layout
 */
enum class WindowSizeClass {
    COMPACT,  // Phone in portrait
    MEDIUM,   // Phone in landscape or small tablet
    EXPANDED  // Large tablet
}

/**
 * Screen orientation
 */
enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * Window size information
 */
data class WindowSize(
    val sizeClass: WindowSizeClass,
    val orientation: ScreenOrientation,
    val width: Dp,
    val height: Dp
)

/**
 * Get current window size information
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp
    val height = configuration.screenHeightDp.dp
    
    val orientation = if (width < height) {
        ScreenOrientation.PORTRAIT
    } else {
        ScreenOrientation.LANDSCAPE
    }
    
    val sizeClass = when {
        width < 600.dp -> WindowSizeClass.COMPACT
        width < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
    
    return WindowSize(
        sizeClass = sizeClass,
        orientation = orientation,
        width = width,
        height = height
    )
}
