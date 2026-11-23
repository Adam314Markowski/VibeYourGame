package com.byd.workout.timer

import android.content.Context
import android.os.SystemClock
import com.byd.workout.timer.TimerService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimerController(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val KEY_TOTAL = intPreferencesKey("timer_total")
    private val KEY_REMAINING = doublePreferencesKey("timer_remaining")
    private val KEY_RUNNING = booleanPreferencesKey("timer_running")
    private val KEY_END_AT = longPreferencesKey("timer_end_at")

    init {
        scope.launch {
            val prefs = dataStore.data.first()
            val total = prefs[KEY_TOTAL] ?: 120
            val remaining = prefs[KEY_REMAINING] ?: 120.0
            val running = prefs[KEY_RUNNING] ?: false
            val endAt = prefs[KEY_END_AT] ?: 0L

            var restoredState = TimerState(
                totalSec = total,
                remainingSec = remaining,
                running = running,
                endAtElapsed = endAt
            )

            // Auto-pause if it expired while app was dead
            if (running && restoredState.remainingAt(SystemClock.elapsedRealtime()) <= 0) {
                restoredState = restoredState.copy(running = false, remainingSec = 0.0)
            }

            _state.value = restoredState
            saveState(restoredState)
        }
    }

    private fun updateState(transform: (TimerState) -> TimerState) {
        val newState = transform(_state.value)
        _state.value = newState
        scope.launch {
            saveState(newState)
        }
    }

    private suspend fun saveState(state: TimerState) {
        dataStore.edit { prefs ->
            prefs[KEY_TOTAL] = state.totalSec
            prefs[KEY_REMAINING] = state.remainingSec
            prefs[KEY_RUNNING] = state.running
            prefs[KEY_END_AT] = state.endAtElapsed
        }
    }

    fun start() {
        updateState { it.start(SystemClock.elapsedRealtime()) }
        TimerService.start(context)
    }

    fun pause() {
        updateState { it.pause(SystemClock.elapsedRealtime()) }
        TimerService.stop(context)
    }

    fun reset() {
        updateState { it.reset() }
        TimerService.stop(context)
    }

    fun adjust(deltaSec: Int) {
        updateState { it.adjust(deltaSec, SystemClock.elapsedRealtime()) }
    }

    fun onTimeUp() {
        updateState { it.copy(remainingSec = 0.0, running = false) }
    }
}
