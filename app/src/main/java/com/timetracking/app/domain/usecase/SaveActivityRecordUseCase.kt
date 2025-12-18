package com.timetracking.app.domain.usecase

import com.timetracking.app.data.local.entity.ActivityRecordEntity
import com.timetracking.app.data.repository.ActivityRecordRepository
import javax.inject.Inject

class SaveActivityRecordUseCase @Inject constructor(
    private val activityRecordRepository: ActivityRecordRepository
) {
    
    suspend operator fun invoke(
        activityId: Long,
        startTime: Long,
        endTime: Long,
        notes: String? = null
    ): Long {
        val totalDuration = endTime - startTime
        
        val record = ActivityRecordEntity(
            activityId = activityId,
            startTime = startTime,
            endTime = endTime,
            totalDuration = totalDuration,
            notes = notes
        )
        
        return activityRecordRepository.insertRecord(record)
    }
}
