package com.timetracking.app.data.repository

import com.timetracking.app.data.local.dao.ActivityDao
import com.timetracking.app.data.local.dao.ActivityRecordDao
import com.timetracking.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val activityRecordDao: ActivityRecordDao
) : ActivityRepository {

    override fun getAllActivities(): Flow<List<ActivityEntity>> {
        return activityDao.getAllActivities()
    }

    override suspend fun getActivityById(activityId: Long): ActivityEntity? {
        return activityDao.getActivityById(activityId)
    }

    override suspend fun insertActivity(activity: ActivityEntity): Long {
        return activityDao.insertActivity(activity)
    }

    override suspend fun updateActivity(activity: ActivityEntity) {
        activityDao.updateActivity(activity)
    }

    override suspend fun deleteActivity(activityId: Long) {
        activityDao.softDeleteActivity(activityId)
    }

    override suspend fun getTotalDurationByActivity(activityId: Long): Long {
        return activityRecordDao.getTotalDurationByActivity(activityId).first() ?: 0L
    }
}
