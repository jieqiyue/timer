package com.timetracking.app.presentation.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedActivityId by viewModel.selectedActivityId.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val yearHeatmapData by viewModel.yearHeatmapData.collectAsState()
    val dailyStatistics by viewModel.dailyStatistics.collectAsState()
    val dailyRecords by viewModel.dailyRecords.collectAsState()
    val monthRecords by viewModel.monthRecords.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val gradientBrush = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "统计分析",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "查看你的时间分布",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // View mode tabs with better styling
            TabRow(
                selectedTabIndex = viewMode.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = viewMode == ViewMode.YEAR,
                    onClick = { viewModel.onViewModeChanged(ViewMode.YEAR) },
                    text = { 
                        Text(
                            text = "年度",
                            fontWeight = if (viewMode == ViewMode.YEAR) 
                                FontWeight.Bold 
                            else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = viewMode == ViewMode.MONTH,
                    onClick = { viewModel.onViewModeChanged(ViewMode.MONTH) },
                    text = { 
                        Text(
                            text = "月度",
                            fontWeight = if (viewMode == ViewMode.MONTH) 
                                FontWeight.Bold 
                            else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = viewMode == ViewMode.DAY,
                    onClick = { viewModel.onViewModeChanged(ViewMode.DAY) },
                    text = { 
                        Text(
                            text = "日度",
                            fontWeight = if (viewMode == ViewMode.DAY) 
                                FontWeight.Bold 
                            else FontWeight.Normal
                        )
                    }
                )
            }

            // Activity filter dropdown with better styling
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                ActivityFilterDropdown(
                    activities = activities,
                    selectedActivityId = selectedActivityId,
                    onActivitySelected = { viewModel.onActivityFilterChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp) // 统一的水平和垂直间距
                )
            }

            // Content area
            when (viewMode) {
                ViewMode.YEAR -> {
                    val heatmapData = yearHeatmapData
                    if (heatmapData != null) {
                        YearView(
                            yearHeatmapData = heatmapData,
                            onDayClick = { timestamp ->
                                viewModel.onDaySelected(timestamp)
                            }
                        )
                    } else {
                        Text(
                            text = "加载中...",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                ViewMode.MONTH -> {
                    MonthView(
                        year = selectedYear,
                        month = selectedMonth,
                        records = monthRecords,
                        activities = activities,
                        onDayClick = { timestamp ->
                            viewModel.onDaySelected(timestamp)
                        }
                    )
                }
                ViewMode.DAY -> {
                    val stats = dailyStatistics
                    if (stats != null) {
                        DayView(
                            dailyStatistics = stats,
                            records = dailyRecords,
                            activities = activities
                        )
                    } else {
                        Text(
                            text = "加载中...",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityFilterDropdown(
    activities: List<com.timetracking.app.domain.model.Activity>,
    selectedActivityId: Long?,
    onActivitySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedActivity = activities.find { it.id == selectedActivityId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedActivity?.name ?: "全部活动",
            onValueChange = {},
            readOnly = true,
            label = { Text("筛选活动") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "全部活动",
                            fontWeight = if (selectedActivityId == null) 
                                FontWeight.Bold 
                            else FontWeight.Normal
                        )
                    }
                },
                onClick = {
                    onActivitySelected(null)
                    expanded = false
                }
            )
            activities.forEach { activity ->
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        Color(activity.color),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = activity.name,
                                fontWeight = if (selectedActivityId == activity.id) 
                                    FontWeight.Bold 
                                else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onActivitySelected(activity.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
