package com.timetracking.app.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.timetracking.app.data.local.database.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ActivityRepositoryImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: ActivityRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = ActivityRepositoryImpl(
            activityDao = database.activityDao(),
            activityRecordDao = database.activityRecordDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should create activity and return id`() = runTest {
        // When
        val activityId = repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")

        // Then
        assertTrue(activityId > 0)
        
        val activity = repository.getActivityById(activityId)
        assertNotNull(activity)
        assertEquals("练琴", activity.name)
        assertEquals(0xFF4CAF50.toInt(), activity.color)
        assertEquals("piano", activity.iconName)
    }

    @Test
    fun `should get all activities`() = runTest {
        // Given
        repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")
        repository.createActivity("健身", 0xFF2196F3.toInt(), "fitness")

        // When
        val activities = repository.getAllActivities().first()

        // Then
        assertEquals(2, activities.size)
        assertEquals("健身", activities[0].name) // Most recent first
        assertEquals("练琴", activities[1].name)
    }

    @Test
    fun `should update activity`() = runTest {
        // Given
        val activityId = repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")
        val activity = repository.getActivityById(activityId)!!

        // When
        val updatedActivity = activity.copy(name = "钢琴练习", color = 0xFFFF5722.toInt())
        repository.updateActivity(updatedActivity)

        // Then
        val result = repository.getActivityById(activityId)
        assertNotNull(result)
        assertEquals("钢琴练习", result.name)
        assertEquals(0xFFFF5722.toInt(), result.color)
    }

    @Test
    fun `should soft delete activity`() = runTest {
        // Given
        val activityId = repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")

        // When
        repository.deleteActivity(activityId)

        // Then
        val activities = repository.getAllActivities().first()
        assertEquals(0, activities.size) // Soft deleted activities should not appear
    }

    @Test
    fun `should get activity with total duration`() = runTest {
        // Given
        val activityId = repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")
        
        // Add some records
        val recordDao = database.activityRecordDao()
        val startTime = System.currentTimeMillis()
        recordDao.insertRecord(
            com.timetracking.app.data.local.entity.ActivityRecordEntity(
                activityId = activityId,
                startTime = startTime,
                endTime = startTime + 3600000L,
                totalDuration = 3600000L
            )
        )
        recordDao.insertRecord(
            com.timetracking.app.data.local.entity.ActivityRecordEntity(
                activityId = activityId,
                startTime = startTime + 7200000L,
                endTime = startTime + 10800000L,
                totalDuration = 3600000L
            )
        )

        // When
        val activityWithDuration = repository.getActivityWithTotalDuration(activityId).first()

        // Then
        assertEquals("练琴", activityWithDuration.activity.name)
        assertEquals(7200000L, activityWithDuration.totalDuration) // 2 hours
    }

    @Test
    fun `should get all activities with duration`() = runTest {
        // Given
        val activity1Id = repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")
        val activity2Id = repository.createActivity("健身", 0xFF2196F3.toInt(), "fitness")
        
        val recordDao = database.activityRecordDao()
        val startTime = System.currentTimeMillis()
        
        // Add records for activity 1
        recordDao.insertRecord(
            com.timetracking.app.data.local.entity.ActivityRecordEntity(
                activityId = activity1Id,
                startTime = startTime,
                endTime = startTime + 3600000L,
                totalDuration = 3600000L
            )
        )
        
        // Add records for activity 2
        recordDao.insertRecord(
            com.timetracking.app.data.local.entity.ActivityRecordEntity(
                activityId = activity2Id,
                startTime = startTime,
                endTime = startTime + 7200000L,
                totalDuration = 7200000L
            )
        )

        // When
        val activitiesWithDuration = repository.getAllActivitiesWithDuration().first()

        // Then
        assertEquals(2, activitiesWithDuration.size)
        
        val fitness = activitiesWithDuration.find { it.activity.name == "健身" }
        assertNotNull(fitness)
        assertEquals(7200000L, fitness.totalDuration)
        
        val piano = activitiesWithDuration.find { it.activity.name == "练琴" }
        assertNotNull(piano)
        assertEquals(3600000L, piano.totalDuration)
    }

    @Test
    fun `should return zero duration for activity with no records`() = runTest {
        // Given
        val activityId = repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")

        // When
        val activityWithDuration = repository.getActivityWithTotalDuration(activityId).first()

        // Then
        assertEquals(0L, activityWithDuration.totalDuration)
    }

    @Test
    fun `should return null for non-existent activity`() = runTest {
        // When
        val activity = repository.getActivityById(999L)

        // Then
        assertNull(activity)
    }

    @Test
    fun `should handle multiple activities with same name`() = runTest {
        // Given
        repository.createActivity("练琴", 0xFF4CAF50.toInt(), "piano")
        repository.createActivity("练琴", 0xFF2196F3.toInt(), "piano2")

        // When
        val activities = repository.getAllActivities().first()

        // Then
        assertEquals(2, activities.size)
        // Both should exist with different IDs
        assertTrue(activities[0].id != activities[1].id)
    }
}
