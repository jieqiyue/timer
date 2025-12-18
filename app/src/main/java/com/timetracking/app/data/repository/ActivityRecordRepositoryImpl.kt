package com.timetracking.app.data.repository

import com.timetracking.app.data.local.dao.ActivityRecordDao
import com.timetracking.app.data.local.entity.ActivityRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ActivityRecordRepositoryImpl @Inject constructor(
    private val activityRecordDao: ActivityRecordDao
) : ActivityRecordRepository {

    override suspend fun insertRecord(record: ActivityRecordEntity): Long {
        return activityRecordDao.insertRecord(record)
    }

    override fun getRecordsByActivity(activityId: Long): Flow<List<ActivityRecordEntity>> {
        return activityRecordDao.getRecordsByActivity(activityId)
    }

    override fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityRecordEntity>> {
        return activityRecordDao.getRecordsByDateRange(startTime, endTime)
    }

    override fun getRecordsByDay(timestamp: Long): Flow<List<ActivityRecordEntity>> {
        return activityRecordDao.getRecordsByDay(timestamp)
    }

    override fun getTotalDurationByActivity(activityId: Long): Flow<Long?> {
        return activityRecordDao.getTotalDurationByActivity(activityId)
    }
}
