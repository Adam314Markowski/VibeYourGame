package com.byd.workout.ui.music

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.byd.workout.R
import com.byd.workout.data.db.MediaItemEntity
import com.byd.workout.ui.bydViewModel
import com.byd.workout.ui.components.BydCard
import com.byd.workout.ui.components.EmptyText
import com.byd.workout.ui.components.ScreenTitle
import com.byd.workout.ui.theme.BydShapes
import com.byd.workout.ui.theme.BydSpacing
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import com.byd.workout.util.formatTimer

@Composable
fun MusicScreen() {
    val viewModel: MusicViewModel = bydViewModel { c ->
        MusicViewModel(c.mediaRepository, c.appContext)
    }

    val items by viewModel.items.collectAsStateWithLifecycle()
    val currentMediaId by viewModel.currentMediaId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val controller by viewModel.controllerState.collectAsStateWithLifecycle()

    var itemToRemove by remember { mutableStateOf<MediaItemEntity?>(null) }

    itemToRemove?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            title = { Text(text = stringResource(R.string.music_remove_confirm)) },
            text = { Text(text = item.title) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.remove(item)
                    itemToRemove = null
                }) {
                    Text(text = stringResource(R.string.music_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BydSpacing.screen)
    ) {
        Spacer(Modifier.height(12.dp))
        ScreenTitle(stringResource(R.string.music_title))
        Text(
            text = stringResource(R.string.music_subtitle),
            style = BydType.body.copy(fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp)),
            color = BydTheme.colors.muted,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Add Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BydTheme.colors.line, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(BydTheme.colors.surface)
                .clickable {
                    launcher.launch(arrayOf("audio/*", "video/*"))
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+",
                    style = BydType.button,
                    color = BydTheme.colors.muted,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.music_add),
                    style = BydType.button,
                    color = BydTheme.colors.muted
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        
        val loadedItem = items.find { it.id.toString() == currentMediaId }
        if (loadedItem?.type == "video" && controller != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.Black),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = true
                        player = controller
                    }
                },
                update = { view ->
                    if (view.player != controller) {
                        view.player = controller
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        if (items.isEmpty()) {
            EmptyText(stringResource(R.string.music_empty))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    val isLoaded = item.id.toString() == currentMediaId
                    MusicItemRow(
                        item = item,
                        isLoaded = isLoaded,
                        isPlaying = isLoaded && isPlaying,
                        onClick = { viewModel.playTrack(item) },
                        onLongClick = { itemToRemove = item }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MusicItemRow(
    item: MediaItemEntity,
    isLoaded: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = BydTheme.colors

    // Using foundation's combinedClickable for long press
    BydCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isLoaded) colors.accent else colors.surface2),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else if (item.type == "video") Icons.Outlined.Movie else Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = if (isLoaded) colors.onAccent else colors.muted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = BydType.rowName,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.music_from_device),
                        style = BydType.small,
                        color = colors.muted
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (item.type == "video") stringResource(R.string.music_badge_video) else stringResource(R.string.music_badge_audio),
                        style = BydType.tiny,
                        color = colors.muted,
                        modifier = Modifier
                            .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Duration
            Text(
                text = if (item.durationMs > 0) formatTimer(item.durationMs / 1000.0) else "–:––",
                style = BydType.small,
                color = colors.muted
            )
        }
    }
}
