package com.byd.workout.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SessionRepository(private val dataStore: DataStore<Preferences>) {
    private val json = Json { ignoreUnknownKeys = true }

    private val key = stringPreferencesKey("session")

    val state: Flow<SessionState> = dataStore.data.map { decode(it[key]) }.distinctUntilChanged()

    suspend fun update(transform: (SessionState) -> SessionState) {
        dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(SessionState.serializer(), transform(decode(prefs[key])))
        }
    }

    private fun decode(raw: String?): SessionState =
        raw?.let { runCatching { json.decodeFromString<SessionState>(it) }.getOrNull() } ?: SessionState()
}
