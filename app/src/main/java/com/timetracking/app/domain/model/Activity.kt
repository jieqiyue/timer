package com.timetracking.app.domain.model

data class Activity(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val iconName: String,
    val createdAt: Long = System.currentTimeMillis()
)
