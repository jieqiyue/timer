package com.timetracking.app.domain.usecase

import com.timetracking.app.data.repository.ActivityRecordRepository
import com.timetracking.app.domain.model.YearHeatmapData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetYearHeatmapDataUseCase @Inject constructor(
    private val activityRecordRepository: ActivityRecordRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    operator fun invoke(year: Int, activityId: Long? = null): Flow<YearHeatmapData> {
        val calendar = Calendar.getInstance()
        calendar.set(year, 0, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(year, 11, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        
        return activityRecordRepository.getRecordsByDateRange(startTime, endTime).map { records ->
            val filteredRecords = if (activityId != null) {
                records.filter { it.activityId == activityId }
            } else {
                records
            }
            
            val dailyData = filteredRecords.groupBy { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.startTime
                dateFormat.format(cal.time)
            }.mapValues { (_, records) ->
                records.sumOf { it.totalDuration }
            }
            
            YearHeatmapData(
                year = year,
                dailyData = dailyData
            )
        }
    }
}
