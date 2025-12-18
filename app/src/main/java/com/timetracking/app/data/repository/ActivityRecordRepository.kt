package com.timetracking.app.data.repository

import com.timetracking.app.data.local.entity.ActivityRecordEntity
import kotlinx.coroutines.flow.Flow

interface ActivityRecordRepository {
    suspend fun insertRecord(record: ActivityRecordEntity): Long
    fun getRecordsByActivity(activityId: Long): Flow<List<ActivityRecordEntity>>
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityRecordEntity>>
    fun getRecordsByDay(timestamp: Long): Flow<List<ActivityRecordEntity>>
    fun getTotalDurationByActivity(activityId: Long): Flow<Long?>
}
