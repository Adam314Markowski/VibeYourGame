package com.byd.workout.data.export

import com.byd.workout.data.LogDto
import com.byd.workout.util.dayIndexOf
import com.byd.workout.util.parseIsoDateOrNull
import com.byd.workout.util.toIsoString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate

enum class SharePreset { ThisWeek, ThisMonth, Last30Days, AllTime }

sealed interface RangeSummary {
    data object InvalidRange : RangeSummary
    data object Empty : RangeSummary
    data class Counts(val days: Int, val exercises: Int) : RangeSummary
}

sealed interface ParsedImport {
    data object NotBydExport : ParsedImport

    data class Valid(val days: Map<LocalDate, List<LogDto>>, val latestDate: LocalDate?) : ParsedImport
}

object LogExport {
    @OptIn(ExperimentalSerializationApi::class)
    private val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun presetRange(preset: SharePreset, today: LocalDate, earliestLogDate: LocalDate?): Pair<LocalDate, LocalDate> {
        val from = when (preset) {
            SharePreset.ThisWeek -> today.minusDays(dayIndexOf(today).toLong())
            SharePreset.ThisMonth -> today.withDayOfMonth(1)
            SharePreset.Last30Days -> today.minusDays(29)
            SharePreset.AllTime -> earliestLogDate ?: today
        }
        return from to today
    }

    fun daysInRange(logs: Map<LocalDate, List<LogDto>>, from: LocalDate, to: LocalDate): Map<LocalDate, List<LogDto>> =
        logs.filter { (date, entries) -> entries.isNotEmpty() && !date.isBefore(from) && !date.isAfter(to) }
            .toSortedMap()

    fun summarize(logs: Map<LocalDate, List<LogDto>>, from: LocalDate?, to: LocalDate?): RangeSummary {
        if (from == null || to == null || from.isAfter(to)) return RangeSummary.InvalidRange
        val days = daysInRange(logs, from, to)
        if (days.isEmpty()) return RangeSummary.Empty
        return RangeSummary.Counts(days = days.size, exercises = days.values.sumOf { it.size })
    }

    fun buildExportJson(logs: Map<LocalDate, List<LogDto>>, from: LocalDate, to: LocalDate): String {
        val root = buildJsonObject {
            put("app", "BYD")
            put("version", 1)
            put("from", from.toIsoString())
            put("to", to.toIsoString())
            putJsonObject("days") {
                daysInRange(logs, from, to).forEach { (date, entries) ->
                    putJsonArray(date.toIsoString()) {
                        entries.forEach { entry ->
                            add(buildJsonObject {
                                put("name", entry.name)
                                put("weight", entry.weight)
                                put("sets", buildJsonArray { entry.sets.forEach { add(JsonPrimitive(it)) } })
                            })
                        }
                    }
                }
            }
        }
        return prettyJson.encodeToString(JsonObject.serializer(), root)
    }

    fun exportFileName(from: LocalDate, to: LocalDate): String = "byd-logs_${from.toIsoString()}_${to.toIsoString()}.json"

    fun parseImport(text: String): ParsedImport {
        val root = runCatching { Json.parseToJsonElement(text) }.getOrNull() as? JsonObject
            ?: return ParsedImport.NotBydExport
        val app = (root["app"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        val days = root["days"] as? JsonObject
        if (app != "BYD" || days == null) return ParsedImport.NotBydExport

        val result = LinkedHashMap<LocalDate, List<LogDto>>()
        for ((key, value) in days) {
            val date = parseIsoDateOrNull(key) ?: continue
            val entries = value as? JsonArray ?: continue
            result[date] = entries.mapNotNull(::parseEntry)
        }
        val latest = days.keys.mapNotNull(::parseIsoDateOrNull).maxOrNull()
        return ParsedImport.Valid(days = result, latestDate = latest)
    }

    private fun parseEntry(element: JsonElement): LogDto? {
        val obj = element as? JsonObject ?: return null
        val name = (obj["name"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
        val weight = when (val w = obj["weight"]) {
            null, JsonNull -> ""
            is JsonPrimitive -> w.content
            else -> w.toString()
        }
        val sets = (obj["sets"] as? JsonArray)?.map { set ->
            if (set is JsonPrimitive) set.content else set.toString()
        } ?: emptyList()
        return LogDto(name = name, weight = weight, sets = sets)
    }

    fun freshEntries(existing: List<LogDto>, incoming: List<LogDto>): List<LogDto> =
        incoming.filter { it !in existing }
}
