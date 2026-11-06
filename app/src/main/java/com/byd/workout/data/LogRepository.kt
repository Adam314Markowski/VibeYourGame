package com.byd.workout.data

import com.byd.workout.data.db.LogDao
import com.byd.workout.data.db.LogEntryEntity
import com.byd.workout.data.export.LogExport
import com.byd.workout.data.export.ParsedImport
import com.byd.workout.util.toIsoString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class LogRepository(private val logDao: LogDao) {
    fun observeAll(): Flow<Map<LocalDate, List<LogDto>>> = logDao.observeAll().map { rows ->
        rows.groupBy({ LocalDate.parse(it.date) }, { it.toDto() })
    }

    suspend fun getDay(date: LocalDate): List<LogDto> = logDao.getDay(date.toIsoString()).map(LogEntryEntity::toDto)

    suspend fun append(date: LocalDate, entries: List<LogDto>) {
        if (entries.isEmpty()) return
        val isoDate = date.toIsoString()
        logDao.append(
            isoDate,
            entries.map { LogEntryEntity(date = isoDate, position = 0, name = it.name, weight = it.weight, sets = it.sets) },
        )
    }

    suspend fun import(parsed: ParsedImport.Valid): Int {
        var added = 0
        for ((date, incoming) in parsed.days) {
            val fresh = LogExport.freshEntries(getDay(date), incoming)
            if (fresh.isNotEmpty()) {
                append(date, fresh)
                added++
            }
        }
        return added
    }
}
