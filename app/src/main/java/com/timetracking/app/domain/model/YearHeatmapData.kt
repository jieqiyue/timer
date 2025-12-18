package com.timetracking.app.domain.model

data class YearHeatmapData(
    val year: Int,
    val dailyData: Map<String, Long> // date string (YYYY-MM-DD) to total duration in milliseconds
)
