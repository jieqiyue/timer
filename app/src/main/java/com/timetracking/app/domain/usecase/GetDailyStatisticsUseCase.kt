package com.timetracking.app.domain.usecase

import com.timetracking.app.data.repository.ActivityRecordRepository
import com.timetracking.app.domain.model.DailyStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetDailyStatisticsUseCase @Inject constructor(
    private val activityRecordRepository: ActivityRecordRepository
) {
    
    operator fun invoke(timestamp: Long, activityId: Long? = null): Flow<DailyStatistics> {
        return activityRecordRepository.getRecordsByDay(timestamp).map { records ->
            val filteredRecords = if (activityId != null) {
                records.filter { it.activityId == activityId }
            } else {
                records
            }
            
            val totalDuration = filteredRecords.sumOf { it.totalDuration }
            val activityBreakdown = filteredRecords.groupBy { it.activityId }
                .mapValues { (_, records) -> records.sumOf { it.totalDuration } }
            
            DailyStatistics(
                date = getDayStartTimestamp(timestamp),
                totalDuration = totalDuration,
                activityBreakdown = activityBreakdown
            )
        }
    }
    
    private fun getDayStartTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
