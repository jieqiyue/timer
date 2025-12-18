package com.timetracking.app.domain.model

data class DailyStatistics(
    val date: Long, // timestamp of the day
    val totalDuration: Long, // in milliseconds
    val activityBreakdown: Map<Long, Long> // activityId to duration
)
