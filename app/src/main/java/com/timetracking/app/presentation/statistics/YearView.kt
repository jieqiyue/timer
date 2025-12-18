package com.timetracking.app.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timetracking.app.domain.model.YearHeatmapData
import com.timetracking.app.ui.components.formatDuration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun YearView(
    yearHeatmapData: YearHeatmapData,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    
    // Calculate total for the year
    val yearTotal = yearHeatmapData.dailyData.values.sum()
    
    // Get max duration for color intensity calculation
    val maxDuration = yearHeatmapData.dailyData.values.maxOrNull() ?: 1L
    
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year total card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp) // 统一的圆角
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp), // 更大的内边距以突出重要信息
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${yearHeatmapData.year}年总时长",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatDuration(yearTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${yearHeatmapData.dailyData.size} 天有记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Legend
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "少",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                repeat(5) { level ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = getColorForIntensity(level / 4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "多",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Heatmap by month (reverse order, only show past months)
        item {
            val currentCalendar = Calendar.getInstance()
            val currentYear = currentCalendar.get(Calendar.YEAR)
            val currentMonth = currentCalendar.get(Calendar.MONTH)
            
            // Determine which months to show
            val monthsToShow = if (yearHeatmapData.year == currentYear) {
                // For current year, only show months up to current month
                (0..currentMonth).toList().reversed()
            } else if (yearHeatmapData.year < currentYear) {
                // For past years, show all months
                (0..11).toList().reversed()
            } else {
                // For future years, show no months
                emptyList()
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                monthsToShow.forEach { month ->
                    MonthHeatmap(
                        year = yearHeatmapData.year,
                        month = month,
                        dailyData = yearHeatmapData.dailyData,
                        maxDuration = maxDuration,
                        onDayClick = onDayClick
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeatmap(
    year: Int,
    month: Int,
    dailyData: Map<String, Long>,
    maxDuration: Long,
    onDayClick: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp) // 统一的圆角
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // 统一的内边距
        ) {
            Text(
                text = monthFormat.format(calendar.time),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Create rows of 7 days (week view)
            // Get the day of week for the first day of the month (1 = Sunday, 7 = Saturday)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val weeks = (daysInMonth + firstDayOfWeek - 1) / 7 + 1
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(weeks) { week ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(7) { dayOfWeek ->
                            // Calculate the actual day of month
                            // week * 7 gives us the starting position of this week
                            // dayOfWeek is 0-6 for the days in this week
                            // firstDayOfWeek - 1 is the offset for the first week
                            val dayOfMonth = week * 7 + dayOfWeek - firstDayOfWeek + 2
                            
                            if (dayOfMonth in 1..daysInMonth) {
                                val dayCalendar = Calendar.getInstance()
                                dayCalendar.set(year, month, dayOfMonth)
                                val dateStr = dateFormat.format(dayCalendar.time)
                                val duration = dailyData[dateStr] ?: 0L
                                val intensity = if (maxDuration > 0) duration.toFloat() / maxDuration else 0f
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = getColorForIntensity(intensity),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                onDayClick(dayCalendar.timeInMillis)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Always show day number for better visibility
                                        Text(
                                            text = dayOfMonth.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (intensity > 0.5f) Color.White else 
                                                   if (duration > 0) Color.Black else Color.Gray
                                        )
                                    }
                                    // Debug: show duration if > 0
                                    if (duration > 0) {
                                        Text(
                                            text = "${duration / 60000}m",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7f),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getColorForIntensity(intensity: Float): Color {
    return when {
        intensity == 0f -> Color(0xFFEBEDF0) // 灰色 - 无数据
        intensity > 0f && intensity <= 0.2f -> Color(0xFFD4F1D4) // 非常浅的绿
        intensity > 0.2f && intensity <= 0.4f -> Color(0xFFA8E6A8) // 浅绿
        intensity > 0.4f && intensity <= 0.6f -> Color(0xFF7BC96F) // 中绿
        intensity > 0.6f && intensity <= 0.8f -> Color(0xFF4CAF50) // 深绿
        else -> Color(0xFF2E7D32) // 最深绿
    }
}
