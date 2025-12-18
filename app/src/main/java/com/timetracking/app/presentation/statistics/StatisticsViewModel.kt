package com.timetracking.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracking.app.data.repository.ActivityRecordRepository
import com.timetracking.app.data.repository.ActivityRepository
import com.timetracking.app.domain.model.DailyStatistics
import com.timetracking.app.domain.model.YearHeatmapData
import com.timetracking.app.domain.usecase.GetDailyStatisticsUseCase
import com.timetracking.app.domain.usecase.GetYearHeatmapDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val activityRecordRepository: ActivityRecordRepository,
    private val getDailyStatisticsUseCase: GetDailyStatisticsUseCase,
    private val getYearHeatmapDataUseCase: GetYearHeatmapDataUseCase
) : ViewModel() {

    private val _viewMode = MutableStateFlow(ViewMode.YEAR)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _selectedActivityId = MutableStateFlow<Long?>(null)
    val selectedActivityId: StateFlow<Long?> = _selectedActivityId.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(System.currentTimeMillis())
    val selectedDay: StateFlow<Long> = _selectedDay.asStateFlow()

    private val _yearHeatmapData = MutableStateFlow<YearHeatmapData?>(null)
    val yearHeatmapData: StateFlow<YearHeatmapData?> = _yearHeatmapData.asStateFlow()

    private val _dailyStatistics = MutableStateFlow<DailyStatistics?>(null)
    val dailyStatistics: StateFlow<DailyStatistics?> = _dailyStatistics.asStateFlow()

    private val _dailyRecords = MutableStateFlow<List<com.timetracking.app.data.local.entity.ActivityRecordEntity>>(emptyList())
    val dailyRecords: StateFlow<List<com.timetracking.app.data.local.entity.ActivityRecordEntity>> = _dailyRecords.asStateFlow()

    private val _monthRecords = MutableStateFlow<List<com.timetracking.app.data.local.entity.ActivityRecordEntity>>(emptyList())
    val monthRecords: StateFlow<List<com.timetracking.app.data.local.entity.ActivityRecordEntity>> = _monthRecords.asStateFlow()

    private val _activities = MutableStateFlow<List<com.timetracking.app.domain.model.Activity>>(emptyList())
    val activities: StateFlow<List<com.timetracking.app.domain.model.Activity>> = _activities.asStateFlow()

    init {
        loadActivities()
        loadYearData()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            activityRepository.getAllActivities().collect { entities ->
                _activities.value = entities.map { entity ->
                    com.timetracking.app.domain.model.Activity(
                        id = entity.id,
                        name = entity.name,
                        color = entity.color,
                        iconName = entity.iconName,
                        createdAt = entity.createdAt
                    )
                }
            }
        }
    }

    fun onViewModeChanged(mode: ViewMode) {
        _viewMode.value = mode
        when (mode) {
            ViewMode.YEAR -> loadYearData()
            ViewMode.MONTH -> loadMonthData()
            ViewMode.DAY -> loadDayData()
        }
    }

    fun onActivityFilterChanged(activityId: Long?) {
        _selectedActivityId.value = activityId
        when (_viewMode.value) {
            ViewMode.YEAR -> loadYearData()
            ViewMode.MONTH -> loadMonthData()
            ViewMode.DAY -> loadDayData()
        }
    }

    fun onYearChanged(year: Int) {
        _selectedYear.value = year
        loadYearData()
    }

    fun onMonthChanged(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        loadMonthData()
    }

    fun onDaySelected(timestamp: Long) {
        _selectedDay.value = timestamp
        _viewMode.value = ViewMode.DAY
        loadDayData()
    }

    private fun loadYearData() {
        viewModelScope.launch {
            getYearHeatmapDataUseCase(
                year = _selectedYear.value,
                activityId = _selectedActivityId.value
            ).collect { data ->
                _yearHeatmapData.value = data
            }
        }
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(_selectedYear.value, _selectedMonth.value, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endTime = calendar.timeInMillis
            
            activityRecordRepository.getRecordsByDateRange(startTime, endTime).collect { records ->
                val filteredRecords = if (_selectedActivityId.value != null) {
                    records.filter { it.activityId == _selectedActivityId.value }
                } else {
                    records
                }
                _monthRecords.value = filteredRecords
            }
        }
    }

    private fun loadDayData() {
        viewModelScope.launch {
            getDailyStatisticsUseCase(_selectedDay.value, _selectedActivityId.value).collect { stats ->
                _dailyStatistics.value = stats
            }
        }
        viewModelScope.launch {
            activityRecordRepository.getRecordsByDay(_selectedDay.value).collect { records ->
                val filteredRecords = if (_selectedActivityId.value != null) {
                    records.filter { it.activityId == _selectedActivityId.value }
                } else {
                    records
                }
                _dailyRecords.value = filteredRecords
            }
        }
    }
}

enum class ViewMode {
    YEAR, MONTH, DAY
}
