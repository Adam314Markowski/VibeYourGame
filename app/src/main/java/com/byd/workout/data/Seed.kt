package com.byd.workout.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.byd.workout.data.db.PlanDao
import com.byd.workout.util.bydHash
import com.byd.workout.util.dayIndexOf
import com.byd.workout.util.toIsoString
import kotlinx.coroutines.flow.first
import java.time.LocalDate

object Seed {
    private val SEEDED = booleanPreferencesKey("seeded")

    val defaultWeek: List<List<String>> = listOf(
        listOf("Bench press", "Overhead press", "Dips", "Triceps pushdown"),
        listOf("Deadlift", "Pull-ups", "Barbell row", "Biceps curl"),
        emptyList(),
        listOf("Back squat", "Romanian deadlift", "Walking lunge", "Calf raise"),
        listOf("Bench press", "Incline DB press", "Cable fly", "Overhead press", "Lateral raise", "Triceps pushdown"),
        listOf("Hip openers", "Thoracic rotations", "Plank"),
        emptyList(),
    )

    suspend fun seedIfNeeded(
        planDao: PlanDao,
        logRepository: LogRepository,
        settings: DataStore<Preferences>,
        includeSampleHistory: Boolean,
        today: LocalDate = LocalDate.now(),
    ) {
        if (settings.data.first()[SEEDED] == true || planDao.count() > 0) return
        defaultWeek.forEachIndexed { day, names -> planDao.replaceDay(day, names) }
        if (includeSampleHistory) {
            sampleHistory(defaultWeek, today).forEach { (date, entries) -> logRepository.append(date, entries) }
        }
        settings.edit { it[SEEDED] = true }
    }

    fun sampleHistory(week: List<List<String>>, today: LocalDate): Map<LocalDate, List<LogDto>> {
        val history = LinkedHashMap<LocalDate, List<LogDto>>()
        for (back in 42 downTo 1) {
            val date = today.minusDays(back.toLong())
            val iso = date.toIsoString()
            val day = week[dayIndexOf(date)]
            if (day.isEmpty() || bydHash(iso) % 5u == 0u) continue
            history[date] = day.map { name ->
                val h = bydHash(name + iso)
                val weight = 10u + (bydHash(name) % 9u) * 10u
                val reps = 6u + h % 7u
                val setCount = (2u + h % 3u).toInt()
                val sets = List(setCount) { s -> "${reps - (if (s > 1) 1u else 0u)} / $weight" }
                LogDto(name = name, weight = if (h % 2u == 1u) "$weight" else "", sets = sets)
            }
        }
        return history
    }
}
