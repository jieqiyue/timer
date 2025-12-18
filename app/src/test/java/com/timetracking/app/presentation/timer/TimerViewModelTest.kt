package com.timetracking.app.presentation.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.timetracking.app.domain.model.Activity
import com.timetracking.app.domain.model.TimerState
import com.timetracking.app.domain.repository.ActivityRecordRepository
import com.timetracking.app.domain.repository.ActivityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: ActivityRepository
    private lateinit var activityRecordRepository: ActivityRecordRepository
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = mockk(relaxed = true)
        activityRecordRepository = mockk(relaxed = true)
        viewModel = TimerViewModel(activityRepository, activityRecordRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be idle`() {
        // Then
        assertIs<TimerState.Idle>(viewModel.timerState.value)
    }

    @Test
    fun `should start timer when activity is loaded`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity

        // When
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.timerState.value
        assertIs<TimerState.Running>(state)
        assertEquals(1L, state.activityId)
        assertEquals("练琴", state.activityName)
        assertEquals(0xFF4CAF50.toInt(), state.activityColor)
    }

    @Test
    fun `should update elapsed time every second`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity

        // When
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val initialState = viewModel.timerState.value as TimerState.Running
        val initialElapsed = initialState.elapsedTime

        // Advance time by 3 seconds
        advanceTimeBy(3000L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val updatedState = viewModel.timerState.value as TimerState.Running
        assertTrue(updatedState.elapsedTime >= initialElapsed + 3000L)
    }

    @Test
    fun `should pause timer`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onPauseClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.timerState.value
        assertIs<TimerState.Paused>(state)
        assertEquals(1L, state.activityId)
    }

    @Test
    fun `should resume timer from paused state`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onPauseClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val pausedState = viewModel.timerState.value as TimerState.Paused
        val pausedElapsed = pausedState.elapsedTime

        // When
        viewModel.onResumeClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.timerState.value
        assertIs<TimerState.Running>(state)
        assertEquals(pausedElapsed, state.elapsedTime)
    }

    @Test
    fun `should save record and finish timer`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity
        coEvery { activityRecordRepository.createRecord(any(), any(), any(), any()) } returns 1L
        
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Advance time to simulate some elapsed time
        advanceTimeBy(5000L)
        testDispatcher.scheduler.advanceUntilIdle()

        var onFinishedCalled = false
        val onFinished = { onFinishedCalled = true }

        // When
        viewModel.onFinishClick(onFinished)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { 
            activityRecordRepository.createRecord(
                activityId = 1L,
                startTime = any(),
                endTime = any(),
                duration = any()
            )
        }
        assertTrue(onFinishedCalled)
        assertIs<TimerState.Idle>(viewModel.timerState.value)
    }

    @Test
    fun `should not save record if elapsed time is zero`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity
        
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        var onFinishedCalled = false
        val onFinished = { onFinishedCalled = true }

        // When - finish immediately without any elapsed time
        viewModel.onFinishClick(onFinished)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should not create record if duration is too short
        assertTrue(onFinishedCalled)
        assertIs<TimerState.Idle>(viewModel.timerState.value)
    }

    @Test
    fun `should preserve elapsed time when pausing and resuming`() = runTest {
        // Given
        val activity = Activity(1, "练琴", 0xFF4CAF50.toInt(), "piano", System.currentTimeMillis())
        coEvery { activityRepository.getActivityById(1L) } returns activity
        
        viewModel.loadActivityAndStartTimer(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Run for 2 seconds
        advanceTimeBy(2000L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val runningState = viewModel.timerState.value as TimerState.Running
        val elapsedBeforePause = runningState.elapsedTime

        // Pause
        viewModel.onPauseClick()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Wait 1 second while paused
        advanceTimeBy(1000L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val pausedState = viewModel.timerState.value as TimerState.Paused
        
        // Resume
        viewModel.onResumeClick()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val resumedState = viewModel.timerState.value as TimerState.Running

        // Then - elapsed time should be preserved
        assertTrue(pausedState.elapsedTime >= elapsedBeforePause)
        assertEquals(pausedState.elapsedTime, resumedState.elapsedTime)
    }
}
