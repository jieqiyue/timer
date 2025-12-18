package com.timetracking.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.timetracking.app.domain.model.TimerState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "timer_state_prefs",
        Context.MODE_PRIVATE
    )

    fun saveTimerState(state: TimerState) {
        prefs.edit().apply {
            when (state) {
                is TimerState.Running -> {
                    putString(KEY_STATE_TYPE, STATE_RUNNING)
                    putLong(KEY_ACTIVITY_ID, state.activityId)
                    putString(KEY_ACTIVITY_NAME, state.activityName)
                    putInt(KEY_ACTIVITY_COLOR, state.activityColor)
                    putLong(KEY_START_TIME, state.startTime)
                    putLong(KEY_ELAPSED_TIME, state.elapsedTime)
                }
                is TimerState.Paused -> {
                    putString(KEY_STATE_TYPE, STATE_PAUSED)
                    putLong(KEY_ACTIVITY_ID, state.activityId)
                    putString(KEY_ACTIVITY_NAME, state.activityName)
                    putInt(KEY_ACTIVITY_COLOR, state.activityColor)
                    putLong(KEY_START_TIME, state.startTime)
                    putLong(KEY_ELAPSED_TIME, state.elapsedTime)
                }
                is TimerState.Idle -> {
                    clear()
                }
            }
            apply()
        }
    }

    fun restoreTimerState(): TimerState {
        val stateType = prefs.getString(KEY_STATE_TYPE, null) ?: return TimerState.Idle

        return when (stateType) {
            STATE_RUNNING -> {
                TimerState.Running(
                    activityId = prefs.getLong(KEY_ACTIVITY_ID, 0),
                    activityName = prefs.getString(KEY_ACTIVITY_NAME, "") ?: "",
                    activityColor = prefs.getInt(KEY_ACTIVITY_COLOR, 0),
                    startTime = prefs.getLong(KEY_START_TIME, 0),
                    elapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0)
                )
            }
            STATE_PAUSED -> {
                TimerState.Paused(
                    activityId = prefs.getLong(KEY_ACTIVITY_ID, 0),
                    activityName = prefs.getString(KEY_ACTIVITY_NAME, "") ?: "",
                    activityColor = prefs.getInt(KEY_ACTIVITY_COLOR, 0),
                    startTime = prefs.getLong(KEY_START_TIME, 0),
                    elapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0)
                )
            }
            else -> TimerState.Idle
        }
    }

    fun clearTimerState() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_STATE_TYPE = "state_type"
        private const val KEY_ACTIVITY_ID = "activity_id"
        private const val KEY_ACTIVITY_NAME = "activity_name"
        private const val KEY_ACTIVITY_COLOR = "activity_color"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_ELAPSED_TIME = "elapsed_time"

        private const val STATE_RUNNING = "running"
        private const val STATE_PAUSED = "paused"
    }
}
