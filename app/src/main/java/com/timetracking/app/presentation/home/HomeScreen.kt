package com.timetracking.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timetracking.app.presentation.theme.MintAccent
import com.timetracking.app.presentation.theme.SkyAccent
import com.timetracking.app.ui.components.ActivityCard
import com.timetracking.app.ui.components.ConfirmDialog
import com.timetracking.app.ui.components.DefaultActivityIcon
import com.timetracking.app.ui.components.formatDuration
import com.timetracking.app.ui.utils.WindowSizeClass
import com.timetracking.app.ui.utils.rememberWindowSize

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onActivityClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val windowSize = rememberWindowSize()

    val gridColumns = when (windowSize.sizeClass) {
        WindowSizeClass.COMPACT -> 2
        WindowSizeClass.MEDIUM -> 3
        WindowSizeClass.EXPANDED -> 4
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.6f)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
            ) {}
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "时间记录",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "让每一分钟都更出彩",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        FilledIconButton(
                            onClick = { showCreateDialog = true },
                            colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "创建")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "创建活动"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HighlightRow()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    when (val state = uiState) {
                        is HomeUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is HomeUiState.Success -> {
                            AnimatedVisibility(visible = state.activities.isNotEmpty()) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(gridColumns),
                                    contentPadding = PaddingValues(
                                        start = 8.dp,
                                        end = 8.dp,
                                        top = 12.dp,
                                        bottom = 88.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(state.activities) { activityWithDuration ->
                                        ActivityCardItem(
                                            activityWithDuration = activityWithDuration,
                                            onClick = { onActivityClick(activityWithDuration.activity.id) },
                                            onLongClick = { viewModel.onActivityLongClick(activityWithDuration.activity.id) }
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = state.activities.isEmpty()) {
                                EmptyStateCard(onCreateClick = { showCreateDialog = true })
                            }
                        }
                        is HomeUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😕",
                                    style = MaterialTheme.typography.displayLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "出错了",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { activityId ->
        ConfirmDialog(
            title = "删除活动",
            message = "确定要删除这个活动吗？所有相关记录也会被删除。",
            onConfirm = { viewModel.onDeleteActivity(activityId) },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    if (showCreateDialog) {
        ActivityDialog(
            title = "创建活动",
            onConfirm = { name, color, iconName ->
                viewModel.onCreateActivity(name, color, iconName)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun HighlightRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HighlightChip(
            title = "快速开始",
            subtitle = "一键启动常用计时",
            colors = Pair(MaterialTheme.colorScheme.primary, SkyAccent),
            icon = Icons.Rounded.Schedule
        )
        HighlightChip(
            title = "效率提升",
            subtitle = "坚持记录形成习惯",
            colors = Pair(MaterialTheme.colorScheme.secondary, MintAccent),
            icon = Icons.Rounded.Bolt
        )
    }
}

@Composable
private fun HighlightChip(
    title: String,
    subtitle: String,
    colors: Pair<Color, Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colors.first.copy(alpha = 0.14f), colors.second.copy(alpha = 0.2f))
                    ),
                    shape = CardDefaults.shape
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(shape = androidx.compose.foundation.shape.CircleShape)
                        .background(colors.first.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = colors.first)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivityCardItem(
    activityWithDuration: com.timetracking.app.domain.model.ActivityWithDuration,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ActivityCard(
        name = activityWithDuration.activity.name,
        color = Color(activityWithDuration.activity.color),
        icon = DefaultActivityIcon,
        totalHours = formatDuration(activityWithDuration.totalDuration),
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@Composable
private fun EmptyStateCard(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = CardDefaults.elevatedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "还没有活动",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角的 + 按钮创建你的第一个活动",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建")
            }
        }
    }
}
