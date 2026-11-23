package com.byd.workout.ui.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byd.workout.R
import com.byd.workout.ui.bydViewModel
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(modifier: Modifier = Modifier) {
    val viewModel: MusicViewModel = bydViewModel { c ->
        MusicViewModel(c.mediaRepository, c.appContext)
    }

    val currentMediaId by viewModel.currentMediaId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val controller by viewModel.controllerState.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()

    if (currentMediaId == null) return

    val loadedItem = items.find { it.id.toString() == currentMediaId }
    val title = loadedItem?.title ?: ""

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(controller, isPlaying) {
        if (isPlaying) {
            while (true) {
                val c = controller
                if (c != null) {
                    val duration = c.duration
                    if (duration > 0) {
                        progress = (c.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    } else {
                        progress = 0f
                    }
                }
                delay(500)
            }
        }
    }

    // if controller is paused, still read it once to show current progress
    LaunchedEffect(controller, currentMediaId) {
        if (!isPlaying) {
            val c = controller
            if (c != null) {
                val duration = c.duration
                if (duration > 0) {
                    progress = (c.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    progress = 0f
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BydTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BydTheme.colors.accent)
                .clickable {
                    controller?.let {
                        if (it.isPlaying) {
                            it.pause()
                        } else {
                            it.play()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = BydTheme.colors.onAccent,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$title · ${stringResource(R.string.music_from_device)}",
                style = BydType.rowName.copy(fontSize = 13.sp), // 13sp 600
                color = BydTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(BydTheme.colors.surface2)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(3.dp)
                        .background(BydTheme.colors.accent)
                )
            }
        }
    }
}
