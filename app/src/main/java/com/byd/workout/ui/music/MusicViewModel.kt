package com.byd.workout.ui.music

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byd.workout.data.MediaRepository
import com.byd.workout.data.db.MediaItemEntity
import com.byd.workout.media.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: MediaRepository,
    private val context: Context
) : ViewModel() {
    val items = repository.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId = _currentMediaId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _controllerState = MutableStateFlow<MediaController?>(null)
    val controllerState = _controllerState.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                val controller = controllerFuture?.get()
                mediaController = controller
                _controllerState.value = controller
                setupControllerListener(controller)
                syncQueue()
            },
            MoreExecutors.directExecutor()
        )

        viewModelScope.launch {
            items.collect {
                syncQueue()
            }
        }
    }

    private fun setupControllerListener(controller: MediaController?) {
        controller?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaId.value = mediaItem?.mediaId
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val duration = controller.duration
                    val currentId = controller.currentMediaItem?.mediaId?.toLongOrNull()
                    if (duration > 0 && currentId != null) {
                        val currentEntity = items.value.find { it.id == currentId }
                        if (currentEntity != null && currentEntity.durationMs == 0L) {
                            viewModelScope.launch {
                                repository.setDuration(currentId, duration)
                            }
                        }
                    }
                }
            }
        })
        _currentMediaId.value = controller?.currentMediaItem?.mediaId
        _isPlaying.value = controller?.isPlaying == true
    }

    private fun syncQueue() {
        val controller = mediaController ?: return
        val currentList = items.value
        val mediaItems = currentList.map { item ->
            MediaItem.Builder()
                .setMediaId(item.id.toString())
                .setUri(item.uri)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(item.title).build())
                .build()
        }

        val currentId = controller.currentMediaItem?.mediaId
        val currentPos = controller.currentPosition
        val isPlayingNow = controller.isPlaying

        controller.setMediaItems(mediaItems)

        val newIndex = currentList.indexOfFirst { it.id.toString() == currentId }
        if (newIndex != -1) {
            controller.seekTo(newIndex, currentPos)
        }
        controller.prepare()
        if (isPlayingNow) {
            controller.play()
        }
    }

    fun playTrack(item: MediaItemEntity) {
        val controller = mediaController ?: return
        val index = items.value.indexOf(item)
        if (index != -1) {
            val isLoaded = controller.currentMediaItem?.mediaId == item.id.toString()
            if (isLoaded) {
                if (controller.isPlaying) {
                    controller.pause()
                } else {
                    controller.play()
                }
            } else {
                controller.seekTo(index, 0)
                controller.play()
            }
        }
    }

    fun addFiles(uris: List<Uri>) {
        viewModelScope.launch {
            repository.add(uris)
        }
    }

    fun remove(item: MediaItemEntity) {
        viewModelScope.launch {
            repository.remove(item)
        }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}

