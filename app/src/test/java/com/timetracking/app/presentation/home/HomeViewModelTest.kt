package com.timetracking.app.presentation.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.timetracking.app.domain.model.Activity
import com.timetracking.app.domain.model.ActivityWithDuration
import com.timetracking.app.domain.repository.ActivityRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: ActivityRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(emptyList())

        // When
        viewModel = HomeViewModel(activityRepository)

        // Then
        assertIs<HomeUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `should load activities successfully`() = runTest {
        // Given
        val activities = listOf(
            ActivityWithDuration(
                activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis()),
                totalDuration = 3600000L
            ),
            ActivityWithDuration(
                activity = Activity(2, "健身", 0xFF2196F3.toInt(), "fitness", System.currentTimeMillis()),
                totalDuration = 7200000L
            )
        )
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(activities)

        // When
        viewModel = HomeViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertIs<HomeUiState.Success>(state)
        assertEquals(2, state.activities.size)
        assertEquals("练琴", state.activities[0].activity.name)
        assertEquals("健身", state.activities[1].activity.name)
    }

    @Test
    fun `should handle empty activities list`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(emptyList())

        // When
        viewModel = HomeViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertIs<HomeUiState.Success>(state)
        assertEquals(0, state.activities.size)
    }

    @Test
    fun `should create activity successfully`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(emptyList())
        coEvery { activityRepository.createActivity(any(), any(), any()) } returns 1L
        viewModel = HomeViewModel(activityRepository)

        // When
        viewModel.onCreateActivity("跑步", 0xFFFF5722.toInt(), "running")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { activityRepository.createActivity("跑步", 0xFFFF5722.toInt(), "running") }
    }

    @Test
    fun `should show delete dialog on long click`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(emptyList())
        viewModel = HomeViewModel(activityRepository)

        // When
        viewModel.onActivityLongClick(1L)

        // Then
        assertEquals(1L, viewModel.showDeleteDialog.value)
    }

    @Test
    fun `should dismiss delete dialog`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(emptyList())
        viewModel = HomeViewModel(activityRepository)
        viewModel.onActivityLongClick(1L)

        // When
        viewModel.dismissDeleteDialog()

        // Then
        assertNull(viewModel.showDeleteDialog.value)
    }

    @Test
    fun `should delete activity and dismiss dialog`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } returns flowOf(emptyList())
        coEvery { activityRepository.deleteActivity(any()) } returns Unit
        viewModel = HomeViewModel(activityRepository)
        viewModel.onActivityLongClick(1L)

        // When
        viewModel.onDeleteActivity(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { activityRepository.deleteActivity(1L) }
        assertNull(viewModel.showDeleteDialog.value)
    }

    @Test
    fun `should handle repository error`() = runTest {
        // Given
        coEvery { activityRepository.getAllActivitiesWithDuration() } throws Exception("Database error")

        // When
        viewModel = HomeViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertIs<HomeUiState.Error>(state)
        assertEquals("Database error", state.message)
    }
}
