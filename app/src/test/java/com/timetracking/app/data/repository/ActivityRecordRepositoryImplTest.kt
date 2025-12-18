package com.timetracking.app.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.timetracking.app.data.local.database.AppDatabase
import com.timetracking.app.data.local.entity.ActivityEntity
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
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ActivityRecordRepositoryImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: ActivityRecordRepositoryImpl
    private var testActivityId: Long = 0

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = ActivityRecordRepositoryImpl(
            activityRecordDao = database.activityRecordDao(),
            activityDao = database.activityDao()
        )

        // Create a test activity
        testActivityId = database.activityDao().insertActivity(
            ActivityEntity(
                name = "练琴",
                color = 0xFF4CAF50.toInt(),
                iconName = "piano",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should create activity record`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()
        val endTime = startTime + 3600000L
        val duration = 3600000L

        // When
        val recordId = repository.createRecord(testActivityId, startTime, endTime, duration)

        // Then
        assertTrue(recordId > 0)
    }

    @Test
    fun `should get records by activity`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()
        repository.createRecord(testActivityId, startTime, startTime + 3600000L, 3600000L)
        repository.createRecord(testActivityId, startTime + 7200000L, startTime + 10800000L, 3600000L)

        // When
        val records = repository.getRecordsByActivity(testActivityId).first()

        // Then
        assertEquals(2, records.size)
        assertEquals("练琴", records[0].activityName)
    }

    @Test
    fun `should get records by date range`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 10, 0, 0) // Jan 15, 2025, 10:00
        val time1 = calendar.timeInMillis
        
        calendar.set(2025, 0, 16, 10, 0, 0) // Jan 16, 2025, 10:00
        val time2 = calendar.timeInMillis
        
        calendar.set(2025, 0, 17, 10, 0, 0) // Jan 17, 2025, 10:00
        val time3 = calendar.timeInMillis

        repository.createRecord(testActivityId, time1, time1 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time2, time2 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time3, time3 + 3600000L, 3600000L)

        // When - get records for Jan 15-16
        calendar.set(2025, 0, 15, 0, 0, 0)
        val startRange = calendar.timeInMillis
        calendar.set(2025, 0, 17, 0, 0, 0)
        val endRange = calendar.timeInMillis

        val records = repository.getRecordsByDateRange(startRange, endRange).first()

        // Then
        assertEquals(2, records.size)
    }

    @Test
    fun `should get records by day`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 10, 0, 0)
        val time1 = calendar.timeInMillis
        
        calendar.set(2025, 0, 15, 14, 0, 0)
        val time2 = calendar.timeInMillis
        
        calendar.set(2025, 0, 16, 10, 0, 0)
        val time3 = calendar.timeInMillis

        repository.createRecord(testActivityId, time1, time1 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time2, time2 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time3, time3 + 3600000L, 3600000L)

        // When - get records for Jan 15
        calendar.set(2025, 0, 15, 12, 0, 0)
        val dayTimestamp = calendar.timeInMillis
        val records = repository.getRecordsByDay(dayTimestamp).first()

        // Then
        assertEquals(2, records.size)
    }

    @Test
    fun `should get daily statistics`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 10, 0, 0)
        val time1 = calendar.timeInMillis
        
        calendar.set(2025, 0, 15, 14, 0, 0)
        val time2 = calendar.timeInMillis

        repository.createRecord(testActivityId, time1, time1 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time2, time2 + 7200000L, 7200000L)

        // When
        calendar.set(2025, 0, 15, 12, 0, 0)
        val dayTimestamp = calendar.timeInMillis
        val statistics = repository.getDailyStatistics(dayTimestamp).first()

        // Then
        assertNotNull(statistics)
        assertEquals(2, statistics.records.size)
        assertEquals(10800000L, statistics.totalDuration) // 3 hours total
    }

    @Test
    fun `should get year heatmap data`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 10, 0, 0)
        val time1 = calendar.timeInMillis
        
        calendar.set(2025, 0, 16, 10, 0, 0)
        val time2 = calendar.timeInMillis
        
        calendar.set(2025, 1, 15, 10, 0, 0) // February
        val time3 = calendar.timeInMillis

        repository.createRecord(testActivityId, time1, time1 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time2, time2 + 7200000L, 7200000L)
        repository.createRecord(testActivityId, time3, time3 + 3600000L, 3600000L)

        // When
        val heatmapData = repository.getYearHeatmapData(2025, null).first()

        // Then
        assertEquals(2025, heatmapData.year)
        assertTrue(heatmapData.dailyData.isNotEmpty())
        assertTrue(heatmapData.dailyData.containsKey("2025-01-15"))
        assertTrue(heatmapData.dailyData.containsKey("2025-01-16"))
        assertTrue(heatmapData.dailyData.containsKey("2025-02-15"))
    }

    @Test
    fun `should filter heatmap data by activity`() = runTest {
        // Given - create another activity
        val activity2Id = database.activityDao().insertActivity(
            ActivityEntity(
                name = "健身",
                color = 0xFF2196F3.toInt(),
                iconName = "fitness",
                createdAt = System.currentTimeMillis()
            )
        )

        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 10, 0, 0)
        val time1 = calendar.timeInMillis

        repository.createRecord(testActivityId, time1, time1 + 3600000L, 3600000L)
        repository.createRecord(activity2Id, time1, time1 + 7200000L, 7200000L)

        // When - filter by first activity
        val heatmapData = repository.getYearHeatmapData(2025, testActivityId).first()

        // Then
        val dayData = heatmapData.dailyData["2025-01-15"]
        assertNotNull(dayData)
        assertEquals(3600000L, dayData) // Only first activity's duration
    }

    @Test
    fun `should sort records by start time`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 14, 0, 0)
        val time1 = calendar.timeInMillis
        
        calendar.set(2025, 0, 15, 10, 0, 0)
        val time2 = calendar.timeInMillis
        
        calendar.set(2025, 0, 15, 12, 0, 0)
        val time3 = calendar.timeInMillis

        // Insert in random order
        repository.createRecord(testActivityId, time1, time1 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time2, time2 + 3600000L, 3600000L)
        repository.createRecord(testActivityId, time3, time3 + 3600000L, 3600000L)

        // When
        calendar.set(2025, 0, 15, 12, 0, 0)
        val dayTimestamp = calendar.timeInMillis
        val records = repository.getRecordsByDay(dayTimestamp).first()

        // Then - should be sorted by start time ascending
        assertEquals(3, records.size)
        assertTrue(records[0].startTime < records[1].startTime)
        assertTrue(records[1].startTime < records[2].startTime)
    }

    @Test
    fun `should handle empty results`() = runTest {
        // When
        val records = repository.getRecordsByActivity(999L).first()

        // Then
        assertEquals(0, records.size)
    }

    @Test
    fun `should calculate correct daily totals`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15, 10, 0, 0)
        val time1 = calendar.timeInMillis
        
        calendar.set(2025, 0, 15, 14, 0, 0)
        val time2 = calendar.timeInMillis
        
        calendar.set(2025, 0, 15, 18, 0, 0)
        val time3 = calendar.timeInMillis

        repository.createRecord(testActivityId, time1, time1 + 1800000L, 1800000L) // 30 min
        repository.createRecord(testActivityId, time2, time2 + 3600000L, 3600000L) // 60 min
        repository.createRecord(testActivityId, time3, time3 + 5400000L, 5400000L) // 90 min

        // When
        calendar.set(2025, 0, 15, 12, 0, 0)
        val dayTimestamp = calendar.timeInMillis
        val statistics = repository.getDailyStatistics(dayTimestamp).first()

        // Then
        assertEquals(10800000L, statistics.totalDuration) // 180 min = 3 hours
    }
}
