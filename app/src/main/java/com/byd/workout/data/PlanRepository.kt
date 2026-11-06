package com.byd.workout.data

import com.byd.workout.data.db.PlanDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlanRepository(private val planDao: PlanDao) {
    fun observeWeek(): Flow<List<List<String>>> = planDao.observeAll().map { items ->
        val byDay = items.groupBy { it.dayIndex }
        List(7) { day -> byDay[day].orEmpty().sortedBy { it.position }.map { it.name } }
    }

    fun observeSuggestions(): Flow<List<String>> = planDao.observeAll().map { items -> suggestionsOf(items.map { it.name }) }

    suspend fun saveDay(dayIndex: Int, names: List<String>) {
        planDao.replaceDay(dayIndex, names.map { it.trim() }.filter { it.isNotEmpty() })
    }

    companion object {
        fun suggestionsOf(names: List<String>): List<String> =
            names.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)

        fun filterSuggestions(suggestions: List<String>, typed: String): List<String> =
            suggestions.filter { it.contains(typed.trim(), ignoreCase = true) }
    }
}
