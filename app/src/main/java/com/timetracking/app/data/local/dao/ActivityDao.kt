package com.timetracking.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.timetracking.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    
    @Insert
    suspend fun insertActivity(activity: ActivityEntity): Long
    
    @Update
    suspend fun updateActivity(activity: ActivityEntity)
    
    @Query("UPDATE activities SET isDeleted = 1 WHERE id = :activityId")
    suspend fun softDeleteActivity(activityId: Long)
    
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): ActivityEntity?
}
