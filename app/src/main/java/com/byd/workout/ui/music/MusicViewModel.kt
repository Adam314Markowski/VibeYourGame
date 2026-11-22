package com.byd.workout.ui.music

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byd.workout.data.MediaRepository
import com.byd.workout.data.db.MediaItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MediaRepository) : ViewModel() {
    val items = repository.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}
