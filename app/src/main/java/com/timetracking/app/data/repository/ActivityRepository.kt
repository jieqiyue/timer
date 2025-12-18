package com.timetracking.app.data.repository

import com.timetracking.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getAllActivities(): Flow<List<ActivityEntity>>
    suspend fun getActivityById(activityId: Long): ActivityEntity?
    suspend fun insertActivity(activity: ActivityEntity): Long
    suspend fun updateActivity(activity: ActivityEntity)
    suspend fun deleteActivity(activityId: Long)
    suspend fun getTotalDurationByActivity(activityId: Long): Long
}
