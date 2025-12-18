package com.timetracking.app.domain.model

data class ActivityWithDuration(
    val activity: Activity,
    val totalDuration: Long // in milliseconds
)
