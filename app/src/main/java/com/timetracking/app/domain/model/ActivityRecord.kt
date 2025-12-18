package com.timetracking.app.domain.model

data class ActivityRecord(
    val id: Long = 0,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long,
    val totalDuration: Long, // in milliseconds
    val notes: String? = null
)
