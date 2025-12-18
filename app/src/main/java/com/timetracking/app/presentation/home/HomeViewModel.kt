package com.timetracking.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracking.app.data.local.entity.ActivityEntity
import com.timetracking.app.data.repository.ActivityRepository
import com.timetracking.app.domain.model.ActivityWithDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<Long?>(null)
    val showDeleteDialog: StateFlow<Long?> = _showDeleteDialog.asStateFlow()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            try {
                activityRepository.getAllActivities()
                    .combine(
                        activityRepository.getAllActivities()
                    ) { activities, _ ->
                        activities.map { activity ->
                            val duration = activityRepository.getTotalDurationByActivity(activity.id)
                            ActivityWithDuration(
                                activity = mapToActivity(activity),
                                totalDuration = duration
                            )
                        }
                    }
                    .collect { activitiesWithDuration ->
                        _uiState.value = HomeUiState.Success(activitiesWithDuration)
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onActivityClick(activityId: Long) {
        // Navigation will be handled by the composable
    }

    fun onActivityLongClick(activityId: Long) {
        _showDeleteDialog.value = activityId
    }

    fun onCreateActivity(name: String, color: Int, iconName: String) {
        viewModelScope.launch {
            try {
                val activity = ActivityEntity(
                    name = name,
                    color = color,
                    iconName = iconName,
                    createdAt = System.currentTimeMillis()
                )
                activityRepository.insertActivity(activity)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to create activity")
            }
        }
    }

    fun onUpdateActivity(activityId: Long, name: String, color: Int, iconName: String) {
        viewModelScope.launch {
            try {
                val activity = activityRepository.getActivityById(activityId)
                if (activity != null) {
                    val updatedActivity = activity.copy(
                        name = name,
                        color = color,
                        iconName = iconName
                    )
                    activityRepository.updateActivity(updatedActivity)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to update activity")
            }
        }
    }

    fun onDeleteActivity(activityId: Long) {
        viewModelScope.launch {
            try {
                activityRepository.deleteActivity(activityId)
                _showDeleteDialog.value = null
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to delete activity")
            }
        }
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    private fun mapToActivity(entity: ActivityEntity): com.timetracking.app.domain.model.Activity {
        return com.timetracking.app.domain.model.Activity(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            iconName = entity.iconName,
            createdAt = entity.createdAt
        )
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val activities: List<ActivityWithDuration>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
