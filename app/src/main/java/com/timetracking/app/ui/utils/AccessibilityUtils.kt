package com.timetracking.app.ui.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Accessibility utilities for ensuring proper touch target sizes
 */
object AccessibilityUtils {
    /**
     * Minimum touch target size according to Material Design guidelines
     * and Android accessibility best practices
     */
    val MIN_TOUCH_TARGET_SIZE: Dp = 48.dp
    
    /**
     * Recommended touch target size for better accessibility
     */
    val RECOMMENDED_TOUCH_TARGET_SIZE: Dp = 56.dp
    
    /**
     * Check if a size meets minimum touch target requirements
     */
    fun meetsMinimumTouchTarget(size: Dp): Boolean {
        return size >= MIN_TOUCH_TARGET_SIZE
    }
    
    /**
     * Check if a size meets recommended touch target requirements
     */
    fun meetsRecommendedTouchTarget(size: Dp): Boolean {
        return size >= RECOMMENDED_TOUCH_TARGET_SIZE
    }
}
