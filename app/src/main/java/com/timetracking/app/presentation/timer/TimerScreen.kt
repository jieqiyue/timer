package com.timetracking.app.presentation.timer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timetracking.app.domain.model.TimerState
import com.timetracking.app.ui.components.formatTime
import com.timetracking.app.ui.utils.ScreenOrientation
import com.timetracking.app.ui.utils.rememberWindowSize

@Composable
fun TimerScreen(
    activityId: Long,
    onFinished: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val windowSize = rememberWindowSize()
    
    // Load activity and start timer when screen is first composed
    androidx.compose.runtime.LaunchedEffect(activityId) {
        viewModel.loadActivityAndStartTimer(activityId)
    }

    val backgroundColor = when (timerState) {
        is TimerState.Running -> Color((timerState as TimerState.Running).activityColor)
        is TimerState.Paused -> Color((timerState as TimerState.Paused).activityColor)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Use different layouts for portrait and landscape
        if (windowSize.orientation == ScreenOrientation.PORTRAIT) {
            TimerPortraitLayout(
                timerState = timerState,
                viewModel = viewModel,
                onFinished = onFinished
            )
        } else {
            TimerLandscapeLayout(
                timerState = timerState,
                viewModel = viewModel,
                onFinished = onFinished
            )
        }
    }
}

@Composable
private fun TimerPortraitLayout(
    timerState: TimerState,
    viewModel: TimerViewModel,
    onFinished: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Activity name
        Text(
            text = when (timerState) {
                is TimerState.Running -> (timerState as TimerState.Running).activityName
                is TimerState.Paused -> (timerState as TimerState.Paused).activityName
                else -> ""
            },
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Timer display
        TimerDisplay(timerState)

        Spacer(modifier = Modifier.height(64.dp))

        // Control buttons
        TimerControls(
            timerState = timerState,
            viewModel = viewModel,
            onFinished = onFinished,
            isLandscape = false
        )
    }
}

@Composable
private fun TimerLandscapeLayout(
    timerState: TimerState,
    viewModel: TimerViewModel,
    onFinished: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Activity name and timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (timerState) {
                    is TimerState.Running -> (timerState as TimerState.Running).activityName
                    is TimerState.Paused -> (timerState as TimerState.Paused).activityName
                    else -> ""
                },
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimerDisplay(timerState)
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Right side: Control buttons
        TimerControls(
            timerState = timerState,
            viewModel = viewModel,
            onFinished = onFinished,
            isLandscape = true
        )
    }
}

@Composable
private fun TimerDisplay(timerState: TimerState) {
    val elapsedTime = when (timerState) {
        is TimerState.Running -> (timerState as TimerState.Running).elapsedTime
        is TimerState.Paused -> (timerState as TimerState.Paused).elapsedTime
        else -> 0L
    }

    // Simple scale animation without infinite repeat
    val scale by animateFloatAsState(
        targetValue = if (timerState is TimerState.Running) 1.0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "timer_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (timerState is TimerState.Running) 1f else 0.7f,
        animationSpec = tween(300),
        label = "timer_alpha"
    )

    // Glassmorphism container for timer
    Box(
        modifier = Modifier
            .background(
                Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(32.dp)
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Text(
            text = formatTime(elapsedTime),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.8f,
                letterSpacing = 4.sp,
                fontFeatureSettings = "tnum"
            ),
            color = Color.White.copy(alpha = alpha),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun TimerControls(
    timerState: TimerState,
    viewModel: TimerViewModel,
    onFinished: () -> Unit,
    isLandscape: Boolean
) {
    val arrangement = if (isLandscape) Arrangement.spacedBy(16.dp) else Arrangement.SpaceEvenly
    
    if (isLandscape) {
        Column(
            verticalArrangement = arrangement,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerControlButtons(timerState, viewModel, onFinished)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = arrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimerControlButtons(timerState, viewModel, onFinished)
        }
    }
}

@Composable
private fun TimerControlButtons(
    timerState: TimerState,
    viewModel: TimerViewModel,
    onFinished: () -> Unit
) {
    // Pause/Resume button with glassmorphism
    when (timerState) {
        is TimerState.Running -> {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { viewModel.onPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "暂停",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        is TimerState.Paused -> {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { viewModel.onResumeClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "继续",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        else -> {}
    }

    // Finish button with glassmorphism
    Box(
        modifier = Modifier
            .size(88.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.25f)
                    )
                ),
                CircleShape
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { viewModel.onFinishClick(onFinished) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "完成",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
        )
    }
}
