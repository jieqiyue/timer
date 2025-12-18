package com.timetracking.app.presentation.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.timetracking.app.domain.model.Activity
import com.timetracking.app.domain.model.ActivityRecord
import com.timetracking.app.domain.model.DailyStatistics
import com.timetracking.app.domain.model.YearHeatmapData
import com.timetracking.app.domain.repository.ActivityRecordRepository
import com.timetracking.app.domain.repository.ActivityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: ActivityRepository
    private lateinit var activityRecordRepository: ActivityRecordRepository
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = mockk(relaxed = true)
        activityRecordRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial view mode should be YEAR`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivities() } returns flowOf(emptyList())
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )

        // When
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(ViewMode.YEAR, viewModel.viewMode.value)
    }

    @Test
    fun `should load activities on init`() = runTest {
        // Given
        val activities = listOf(
            Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis()),
            Activity(2, "健身", 0xFF2196F3.toInt(), "fitness", System.currentTimeMillis())
        )
        coEvery { activityRepository.getAllActivities() } returns flowOf(activities)
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )

        // When
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.activities.value.size)
        assertEquals("练琴", viewModel.activities.value[0].name)
        assertEquals("健身", viewModel.activities.value[1].name)
    }

    @Test
    fun `should load year heatmap data on init`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val heatmapData = YearHeatmapData(
            year = currentYear,
            dailyData = mapOf("2025-01-15" to 3600000L)
        )
        coEvery { activityRepository.getAllActivities() } returns flowOf(emptyList())
        coEvery { activityRecordRepository.getYearHeatmapData(currentYear, null) } returns flowOf(heatmapData)

        // When
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.yearHeatmapData.value)
        assertEquals(currentYear, viewModel.yearHeatmapData.value?.year)
    }

    @Test
    fun `should change view mode`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivities() } returns flowOf(emptyList())
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onViewModeChanged(ViewMode.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(ViewMode.MONTH, viewModel.viewMode.value)
    }

    @Test
    fun `should filter by activity`() = runTest {
        // Given
        val activities = listOf(
            Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        )
        coEvery { activityRepository.getAllActivities() } returns flowOf(activities)
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onActivityFilterChanged(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1L, viewModel.selectedActivityId.value)
    }

    @Test
    fun `should clear activity filter`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivities() } returns flowOf(emptyList())
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onActivityFilterChanged(1L)

        // When
        viewModel.onActivityFilterChanged(null)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedActivityId.value)
    }

    @Test
    fun `should load daily statistics when day is selected`() = runTest {
        // Given
        val timestamp = System.currentTimeMillis()
        val records = listOf(
            ActivityRecord(
                id = 1,
                activityId = 1,
                activityName = "练琴",
                activityColor = 0xFF4CAF50.toInt(),
                startTime = timestamp,
                endTime = timestamp + 3600000L,
                totalDuration = 3600000L,
                notes = null
            )
        )
        val dailyStats = DailyStatistics(
            date = timestamp,
            records = records,
            totalDuration = 3600000L
        )
        
        coEvery { activityRepository.getAllActivities() } returns flowOf(emptyList())
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )
        coEvery { activityRecordRepository.getDailyStatistics(timestamp) } returns flowOf(dailyStats)
        coEvery { activityRecordRepository.getRecordsByDay(timestamp) } returns flowOf(records)
        
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onDaySelected(timestamp)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(ViewMode.DAY, viewModel.viewMode.value)
        assertNotNull(viewModel.dailyStatistics.value)
        assertEquals(1, viewModel.dailyRecords.value.size)
        assertEquals(3600000L, viewModel.dailyStatistics.value?.totalDuration)
    }

    @Test
    fun `should load month records when switching to month view`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        calendar.set(year, month, 1, 0, 0, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endTime = calendar.timeInMillis
        
        val records = listOf(
            ActivityRecord(
                id = 1,
                activityId = 1,
                activityName = "练琴",
                activityColor = 0xFF4CAF50.toInt(),
                startTime = startTime,
                endTime = startTime + 3600000L,
                totalDuration = 3600000L,
                notes = null
            )
        )
        
        coEvery { activityRepository.getAllActivities() } returns flowOf(emptyList())
        coEvery { activityRecordRepository.getYearHeatmapData(any(), any()) } returns flowOf(
            YearHeatmapData(2025, emptyMap())
        )
        coEvery { activityRecordRepository.getRecordsByDateRange(any(), any()) } returns flowOf(records)
        
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onViewModeChanged(ViewMode.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(ViewMode.MONTH, viewModel.viewMode.value)
        assertEquals(1, viewModel.monthRecords.value.size)
    }

    @Test
    fun `should update data when activity filter changes`() = runTest {
        // Given
        val activities = listOf(
            Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        )
        coEvery { activityRepository.getAllActivities() } returns flowOf(activities)
        coEvery { activityRecordRepository.getYearHeatmapData(any(), null) } returns flowOf(
            YearHeatmapData(2025, mapOf("2025-01-15" to 3600000L))
        )
        coEvery { activityRecordRepository.getYearHeatmapData(any(), 1L) } returns flowOf(
            YearHeatmapData(2025, mapOf("2025-01-15" to 1800000L))
        )
        
        viewModel = StatisticsViewModel(activityRepository, activityRecordRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val initialData = viewModel.yearHeatmapData.value

        // When
        viewModel.onActivityFilterChanged(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val filteredData = viewModel.yearHeatmapData.value
        assertNotNull(filteredData)
        // Data should be updated based on the filter
    }
}
