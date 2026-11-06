package com.byd.workout.data

import com.byd.workout.ui.history.HistoryViewModel
import com.byd.workout.util.dayIndexOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeedAndSuggestionsTest {
    private val today = LocalDate.of(2026, 10, 23)

    @Test
    fun sampleHistory_coversOnlyTrainingDaysInTheLast42Days() {
        val history = Seed.sampleHistory(Seed.defaultWeek, today)
        assertTrue(history.isNotEmpty())
        history.forEach { (date, entries) ->
            assertTrue(date.isBefore(today) && !date.isBefore(today.minusDays(42)))
            assertEquals(Seed.defaultWeek[dayIndexOf(date)].map { it }, entries.map { it.name })
            entries.forEach { entry ->
                assertTrue(entry.sets.size in 2..4)
                assertTrue(entry.weight.isEmpty() || entry.weight.toInt() in 10..90)
            }
        }
    }

    @Test
    fun sampleHistory_isDeterministic() =
        assertEquals(Seed.sampleHistory(Seed.defaultWeek, today), Seed.sampleHistory(Seed.defaultWeek, today))

    @Test
    fun suggestions_areDistinctTrimmedSorted() {
        val names = listOf(" Plank", "Bench press", "bench press", "", "Plank ", "Dips")
        assertEquals(listOf("Bench press", "bench press", "Dips", "Plank"), PlanRepository.suggestionsOf(names))
    }

    @Test
    fun suggestions_filterCaseInsensitiveContains() {
        val all = listOf("Bench press", "Incline DB press", "Plank")
        assertEquals(listOf("Bench press", "Incline DB press"), PlanRepository.filterSuggestions(all, "PRESS"))
        assertEquals(emptyList<String>(), PlanRepository.filterSuggestions(all, "xyz"))
    }

    @Test
    fun sessionLogs_followTodaysSavedEntries() {
        val saved = mapOf(today to listOf(LogDto("A", "", listOf("1"))))
        val merged = HistoryViewModel.withSessionLogs(saved, listOf(LogDto("B", "", listOf("2"))), today)
        assertEquals(listOf("A", "B"), merged[today]!!.map { it.name })
        assertEquals(saved, HistoryViewModel.withSessionLogs(saved, emptyList(), today))
    }
}
