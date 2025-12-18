package com.timetracking.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.util.concurrent.TimeUnit

@Composable
fun TimeDisplay(
    milliseconds: Long,
    modifier: Modifier = Modifier
) {
    val formattedTime = formatTime(milliseconds)
    
    Text(
        text = formattedTime,
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

fun formatTime(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun formatDuration(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    
    return if (hours > 0) {
        String.format("%d小时%d分钟", hours, minutes)
    } else {
        String.format("%d分钟", minutes)
    }
}
