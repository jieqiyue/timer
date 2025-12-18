package com.timetracking.app.di

import android.content.Context
import androidx.room.Room
import com.timetracking.app.data.local.dao.ActivityDao
import com.timetracking.app.data.local.dao.ActivityRecordDao
import com.timetracking.app.data.local.database.AppDatabase
import com.timetracking.app.data.repository.ActivityRecordRepository
import com.timetracking.app.data.repository.ActivityRecordRepositoryImpl
import com.timetracking.app.data.repository.ActivityRepository
import com.timetracking.app.data.repository.ActivityRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "time_tracking_database"
        ).build()
    }
    
    @Provides
    fun provideActivityDao(database: AppDatabase): ActivityDao {
        return database.activityDao()
    }
    
    @Provides
    fun provideActivityRecordDao(database: AppDatabase): ActivityRecordDao {
        return database.activityRecordDao()
    }
    
    @Provides
    @Singleton
    fun provideActivityRepository(
        activityDao: ActivityDao,
        activityRecordDao: ActivityRecordDao
    ): ActivityRepository {
        return ActivityRepositoryImpl(activityDao, activityRecordDao)
    }
    
    @Provides
    @Singleton
    fun provideActivityRecordRepository(
        activityRecordDao: ActivityRecordDao
    ): ActivityRecordRepository {
        return ActivityRecordRepositoryImpl(activityRecordDao)
    }
}
