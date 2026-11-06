package com.byd.workout.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BydSnackbar(val hostState: SnackbarHostState, private val scope: CoroutineScope) {
    private var job: Job? = null

    fun show(message: String) {
        job?.cancel()
        hostState.currentSnackbarData?.dismiss()
        job = scope.launch {
            launch { hostState.showSnackbar(message, duration = SnackbarDuration.Indefinite) }
            delay(DURATION_MS)
            hostState.currentSnackbarData?.dismiss()
        }
    }

    companion object {
        const val DURATION_MS = 2_600L
    }
}

val LocalBydSnackbar = staticCompositionLocalOf<BydSnackbar> { error("No BydSnackbar provided") }
