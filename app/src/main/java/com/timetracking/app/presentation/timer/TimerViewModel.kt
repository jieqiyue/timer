package com.timetracking.app.presentation.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracking.app.data.repository.ActivityRepository
import com.timetracking.app.domain.model.TimerState
import com.timetracking.app.domain.usecase.SaveActivityRecordUseCase
import com.timetracking.app.domain.usecase.StartTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val startTimerUseCase: StartTimerUseCase,
    private val saveActivityRecordUseCase: SaveActivityRecordUseCase,
    private val timerStateManager: com.timetracking.app.data.local.TimerStateManager
) : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Try to restore timer state on initialization
        restoreTimerState()
    }

    fun loadActivityAndStartTimer(activityId: Long) {
        viewModelScope.launch {
            try {
                val activity = activityRepository.getActivityById(activityId)
                if (activity != null) {
                    val runningState = startTimerUseCase(
                        activityId = activity.id,
                        activityName = activity.name,
                        activityColor = activity.color
                    )
                    _timerState.value = runningState
                    timerStateManager.saveTimerState(runningState)
                    startTimer()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _timerState.value
                if (currentState is TimerState.Running) {
                    val elapsed = System.currentTimeMillis() - currentState.startTime
                    _timerState.value = currentState.copy(elapsedTime = elapsed)
                }
            }
        }
    }

    private fun restoreTimerState() {
        val savedState = timerStateManager.restoreTimerState()
        if (savedState !is TimerState.Idle) {
            _timerState.value = savedState
            if (savedState is TimerState.Running) {
                startTimer()
            }
        }
    }

    fun onPauseClick() {
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            timerJob?.cancel()
            val pausedState = TimerState.Paused(
                activityId = currentState.activityId,
                activityName = currentState.activityName,
                activityColor = currentState.activityColor,
                startTime = currentState.startTime,
                elapsedTime = currentState.elapsedTime
            )
            _timerState.value = pausedState
            timerStateManager.saveTimerState(pausedState)
        }
    }

    fun onResumeClick() {
        val currentState = _timerState.value
        if (currentState is TimerState.Paused) {
            val newStartTime = System.currentTimeMillis() - currentState.elapsedTime
            val runningState = TimerState.Running(
                activityId = currentState.activityId,
                activityName = currentState.activityName,
                activityColor = currentState.activityColor,
                startTime = newStartTime,
                elapsedTime = currentState.elapsedTime
            )
            _timerState.value = runningState
            timerStateManager.saveTimerState(runningState)
            startTimer()
        }
    }

    fun onFinishClick(onFinished: () -> Unit) {
        viewModelScope.launch {
            val currentState = _timerState.value
            when (currentState) {
                is TimerState.Running, is TimerState.Paused -> {
                    timerJob?.cancel()
                    
                    val activityId = when (currentState) {
                        is TimerState.Running -> currentState.activityId
                        is TimerState.Paused -> currentState.activityId
                        else -> return@launch
                    }
                    
                    val startTime = when (currentState) {
                        is TimerState.Running -> currentState.startTime
                        is TimerState.Paused -> currentState.startTime
                        else -> return@launch
                    }
                    
                    val elapsedTime = when (currentState) {
                        is TimerState.Running -> currentState.elapsedTime
                        is TimerState.Paused -> currentState.elapsedTime
                        else -> return@launch
                    }
                    
                    val endTime = startTime + elapsedTime
                    
                    try {
                        saveActivityRecordUseCase(
                            activityId = activityId,
                            startTime = startTime,
                            endTime = endTime
                        )
                        _timerState.value = TimerState.Idle
                        timerStateManager.clearTimerState()
                        onFinished()
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
                else -> {}
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
