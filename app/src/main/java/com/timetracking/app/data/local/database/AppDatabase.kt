package com.timetracking.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timetracking.app.data.local.dao.ActivityDao
import com.timetracking.app.data.local.dao.ActivityRecordDao
import com.timetracking.app.data.local.entity.ActivityEntity
import com.timetracking.app.data.local.entity.ActivityRecordEntity

@Database(
    entities = [
        ActivityEntity::class,
        ActivityRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun activityRecordDao(): ActivityRecordDao
}
