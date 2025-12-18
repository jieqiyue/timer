package com.timetracking.app.presentation.timer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val timerState by viewModel.timerState.collectAsState()
    val windowSize = rememberWindowSize()

    androidx.compose.runtime.LaunchedEffect(activityId) {
        viewModel.loadActivityAndStartTimer(activityId)
    }

    val backgroundColor = when (timerState) {
        is TimerState.Running -> Color((timerState as TimerState.Running).activityColor)
        is TimerState.Paused -> Color((timerState as TimerState.Paused).activityColor)
        else -> MaterialTheme.colorScheme.primary
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            backgroundColor.copy(alpha = 0.96f),
            backgroundColor.copy(alpha = 0.9f),
            backgroundColor.copy(alpha = 0.72f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        DecorativeGlow()
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
private fun DecorativeGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset((-80).dp, (-120).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(240.dp, 520.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
    }
}

@Composable
private fun TimerPortraitLayout(
    timerState: TimerState,
    viewModel: TimerViewModel,
    onFinished: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (timerState) {
                        is TimerState.Running -> (timerState as TimerState.Running).activityName
                        is TimerState.Paused -> (timerState as TimerState.Paused).activityName
                        else -> "",
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "专注进行中",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        TimerDisplay(timerState)

        Spacer(modifier = Modifier.height(64.dp))

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
    onFinished: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (timerState) {
                    is TimerState.Running -> (timerState as TimerState.Running).activityName
                    is TimerState.Paused -> (timerState as TimerState.Paused).activityName
                    else -> "",
                },
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimerDisplay(timerState)
        }

        Spacer(modifier = Modifier.width(32.dp))

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

    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                RoundedCornerShape(36.dp)
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.28f),
                shape = RoundedCornerShape(36.dp)
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
    isLandscape: Boolean,
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
    onFinished: () -> Unit,
) {
    when (timerState) {
        is TimerState.Running -> {
            GlassButton(onClick = { viewModel.onPauseClick() }) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "暂停",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        is TimerState.Paused -> {
            GlassButton(onClick = { viewModel.onResumeClick() }) {
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

    GlassButton(
        onClick = { viewModel.onFinishClick(onFinished) },
        borderColor = Color.White.copy(alpha = 0.5f),
        backgroundBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.38f),
                Color.White.copy(alpha = 0.2f)
            )
        )
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "完成",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
private fun GlassButton(
    onClick: () -> Unit,
    borderColor: Color = Color.White.copy(alpha = 0.4f),
    backgroundBrush: Brush = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.22f)
        )
    ),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .background(backgroundBrush, CircleShape)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
