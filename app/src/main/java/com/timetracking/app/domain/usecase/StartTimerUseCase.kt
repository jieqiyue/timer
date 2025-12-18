package com.timetracking.app.domain.usecase

import com.timetracking.app.domain.model.TimerState
import javax.inject.Inject

class StartTimerUseCase @Inject constructor() {
    
    operator fun invoke(
        activityId: Long,
        activityName: String,
        activityColor: Int
    ): TimerState.Running {
        return TimerState.Running(
            activityId = activityId,
            activityName = activityName,
            activityColor = activityColor,
            startTime = System.currentTimeMillis(),
            elapsedTime = 0
        )
    }
}
