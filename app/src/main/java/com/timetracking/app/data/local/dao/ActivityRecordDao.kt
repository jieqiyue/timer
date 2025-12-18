package com.timetracking.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.timetracking.app.data.local.entity.ActivityRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityRecordDao {
    @Insert
    suspend fun insertRecord(record: ActivityRecordEntity): Long
    
    @Query("SELECT * FROM activity_records WHERE activityId = :activityId ORDER BY startTime DESC")
    fun getRecordsByActivity(activityId: Long): Flow<List<ActivityRecordEntity>>
    
    @Query("SELECT * FROM activity_records WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityRecordEntity>>
    
    @Query("SELECT SUM(totalDuration) FROM activity_records WHERE activityId = :activityId")
    fun getTotalDurationByActivity(activityId: Long): Flow<Long?>
    
    @Query("SELECT * FROM activity_records WHERE DATE(startTime/1000, 'unixepoch', 'localtime') = DATE(:timestamp/1000, 'unixepoch', 'localtime') ORDER BY startTime ASC")
    fun getRecordsByDay(timestamp: Long): Flow<List<ActivityRecordEntity>>
}
