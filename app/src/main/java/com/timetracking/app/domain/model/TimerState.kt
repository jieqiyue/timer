package com.timetracking.app.domain.model

sealed class TimerState {
    object Idle : TimerState()
    
    data class Running(
        val activityId: Long,
        val activityName: String,
        val activityColor: Int,
        val startTime: Long,
        val elapsedTime: Long = 0
    ) : TimerState()
    
    data class Paused(
        val activityId: Long,
        val activityName: String,
        val activityColor: Int,
        val startTime: Long,
        val elapsedTime: Long
    ) : TimerState()
}
