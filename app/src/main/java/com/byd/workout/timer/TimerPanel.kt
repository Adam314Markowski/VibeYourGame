package com.byd.workout.timer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byd.workout.BydApp
import com.byd.workout.R
import com.byd.workout.ui.theme.BydShapes
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import com.byd.workout.util.formatTimer
import kotlinx.coroutines.delay

@Composable
fun TimerPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val timerController = remember {
        (context.applicationContext as BydApp).container.timerController
    }
    val timerState by timerController.state.collectAsStateWithLifecycle()

    var remaining by remember(timerState.remainingSec) { mutableDoubleStateOf(timerState.remainingSec) }

    // Animate remaining value smoothly when timer is running
    LaunchedEffect(timerState.running, timerState.endAtElapsed) {
        if (timerState.running) {
            while (true) {
                remaining = timerState.remainingAt(android.os.SystemClock.elapsedRealtime())
                delay(200)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dial
        val trackColor = BydTheme.colors.surface2
        val accentColor = BydTheme.colors.accent
        val alphaAnim = remember { Animatable(1f) }
        var hasBlinked by remember { mutableStateOf(false) }

        LaunchedEffect(remaining) {
            if (remaining <= 0.0 && !hasBlinked) {
                hasBlinked = true
                repeat(4) {
                    alphaAnim.animateTo(0.15f, animationSpec = tween(250))
                    alphaAnim.animateTo(1f, animationSpec = tween(250))
                }
            } else if (remaining > 0.0) {
                hasBlinked = false
                alphaAnim.snapTo(1f)
            }
        }

        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (timerState.running) {
                        timerController.pause()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        timerController.start()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeftOffset = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                // Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeftOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                // Progress Arc
                val sweep = if (timerState.totalSec > 0) {
                    (remaining / timerState.totalSec.toDouble()) * 360f
                } else {
                    0.0
                }

                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = sweep.toFloat(),
                    useCenter = false,
                    topLeft = topLeftOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    alpha = alphaAnim.value
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTimer(remaining),
                    style = BydType.timerDigits,
                    color = BydTheme.colors.ink
                )
                Text(
                    text = when {
                        remaining <= 0.0 -> stringResource(R.string.timer_times_up)
                        timerState.running -> stringResource(R.string.timer_tap_to_pause)
                        remaining >= timerState.totalSec -> stringResource(R.string.timer_tap_to_start)
                        else -> stringResource(R.string.timer_paused)
                    },
                    style = BydType.tiny,
                    color = BydTheme.colors.muted,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Adjust buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            val pillModifier = Modifier
                .clip(BydShapes.pill)
                .border(1.dp, BydTheme.colors.line, BydShapes.pill)
                .padding(horizontal = 8.dp, vertical = 3.dp)

            Text(
                text = "−1m",
                style = BydType.button,
                color = BydTheme.colors.ink,
                modifier = Modifier
                    .then(pillModifier)
                    .clickable { timerController.adjust(-60) }
            )
            Text(
                text = "↺",
                style = BydType.button,
                color = BydTheme.colors.ink,
                modifier = Modifier
                    .then(pillModifier)
                    .clickable { timerController.reset() }
            )
            Text(
                text = "+1m",
                style = BydType.button,
                color = BydTheme.colors.ink,
                modifier = Modifier
                    .then(pillModifier)
                    .clickable { timerController.adjust(60) }
            )
        }
    }
}
