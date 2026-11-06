package com.byd.workout.data

import com.byd.workout.data.export.LogExport
import com.byd.workout.data.export.ParsedImport
import com.byd.workout.data.export.RangeSummary
import com.byd.workout.data.export.SharePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LogExportTest {
    private val today = LocalDate.of(2026, 10, 23)
    private val bench = LogDto("Bench press", "60", listOf("8 / 60", "8 / 60"))
    private val squat = LogDto("Back squat", "", listOf("5 / 100"))
    private val logs = mapOf(
        LocalDate.of(2026, 10, 2) to listOf(bench),
        LocalDate.of(2026, 10, 8) to listOf(squat, bench),
        LocalDate.of(2026, 9, 1) to listOf(squat),
    )

    @Test
    fun presets_forFixedToday() {
        assertEquals(LocalDate.of(2026, 10, 19) to today, LogExport.presetRange(SharePreset.ThisWeek, today, null))
        assertEquals(LocalDate.of(2026, 10, 1) to today, LogExport.presetRange(SharePreset.ThisMonth, today, null))
        assertEquals(LocalDate.of(2026, 9, 24) to today, LogExport.presetRange(SharePreset.Last30Days, today, null))
        assertEquals(LocalDate.of(2026, 9, 1) to today, LogExport.presetRange(SharePreset.AllTime, today, LocalDate.of(2026, 9, 1)))
        assertEquals(today to today, LogExport.presetRange(SharePreset.AllTime, today, null))
    }

    @Test
    fun thisWeek_onMonday_startsToday() {
        val monday = LocalDate.of(2026, 10, 19)
        assertEquals(monday to monday, LogExport.presetRange(SharePreset.ThisWeek, monday, null))
    }

    @Test
    fun summary_countsDaysAndExercises() {
        assertEquals(RangeSummary.Counts(days = 2, exercises = 3), LogExport.summarize(logs, LocalDate.of(2026, 10, 1), today))
    }

    @Test
    fun summary_invalidAndEmpty() {
        assertEquals(RangeSummary.InvalidRange, LogExport.summarize(logs, today, LocalDate.of(2026, 10, 1)))
        assertEquals(RangeSummary.InvalidRange, LogExport.summarize(logs, null, today))
        assertEquals(RangeSummary.Empty, LogExport.summarize(logs, LocalDate.of(2026, 10, 20), today))
    }

    @Test
    fun export_hasHeaderAndOnlyDatesInRange_ascending() {
        val json = LogExport.buildExportJson(logs, LocalDate.of(2026, 10, 1), today)
        assertTrue(json.contains("\"app\": \"BYD\""))
        assertTrue(json.contains("\"version\": 1"))
        assertTrue(json.contains("\"from\": \"2026-10-01\""))
        assertTrue(!json.contains("2026-09-01"))
        assertTrue(json.indexOf("2026-10-02") < json.indexOf("2026-10-08"))
        assertTrue("2-space indent", json.lines()[1].startsWith("  \"app\""))
    }

    @Test
    fun exportFileName() =
        assertEquals("byd-logs_2026-10-01_2026-10-23.json", LogExport.exportFileName(LocalDate.of(2026, 10, 1), today))

    private fun importInto(store: MutableMap<LocalDate, List<LogDto>>, text: String): Int {
        val parsed = LogExport.parseImport(text) as ParsedImport.Valid
        var added = 0
        for ((date, incoming) in parsed.days) {
            val fresh = LogExport.freshEntries(store[date].orEmpty(), incoming)
            if (fresh.isNotEmpty()) {
                store[date] = store[date].orEmpty() + fresh
                added++
            }
        }
        return added
    }

    @Test
    fun importingOwnExport_addsNothing() {
        val store = logs.toMutableMap()
        val json = LogExport.buildExportJson(store, LocalDate.of(2026, 1, 1), today)
        assertEquals(0, importInto(store, json))
        assertEquals(logs, store)
    }

    @Test
    fun importingExportWithOneNewDay_importsExactlyOne() {
        val store = logs.toMutableMap()
        val withNewDay = logs + (LocalDate.of(2026, 10, 20) to listOf(squat))
        val json = LogExport.buildExportJson(withNewDay, LocalDate.of(2026, 1, 1), today)
        assertEquals(1, importInto(store, json))
        assertEquals(listOf(squat), store[LocalDate.of(2026, 10, 20)])
    }

    @Test
    fun import_appendsOnlyNewEntriesToExistingDay() {
        val store = logs.toMutableMap()
        val extra = LogDto("Dips", "", listOf("10"))
        val json = LogExport.buildExportJson(mapOf(LocalDate.of(2026, 10, 2) to listOf(bench, extra)), LocalDate.of(2026, 10, 1), today)
        assertEquals(1, importInto(store, json))
        assertEquals(listOf(bench, extra), store[LocalDate.of(2026, 10, 2)])
    }

    @Test
    fun import_rejectsNonBydJson() =
        assertEquals(ParsedImport.NotBydExport, LogExport.parseImport("""{"app":"Other","days":{}}"""))

    @Test
    fun import_rejectsInvalidJson() =
        assertEquals(ParsedImport.NotBydExport, LogExport.parseImport("{not json"))

    @Test
    fun import_rejectsNonObjectDays() {
        assertEquals(ParsedImport.NotBydExport, LogExport.parseImport("""{"app":"BYD","days":[]}"""))
        assertEquals(ParsedImport.NotBydExport, LogExport.parseImport("""{"app":"BYD"}"""))
        assertEquals(ParsedImport.NotBydExport, LogExport.parseImport("""[1,2]"""))
    }

    @Test
    fun import_skipsBadDatesAndEntries_andCoercesValues() {
        val parsed = LogExport.parseImport(
            """
            {"app":"BYD","days":{
              "2026-10-02":[{"name":"Bench press","weight":60,"sets":["8 / 60",8]},{"weight":"1"},"junk",{"name":"Plank"}],
              "02.10.2026":[{"name":"Skipped"}],
              "2026-10-03":"not an array",
              "2026-10-09":[]
            }}
            """.trimIndent(),
        ) as ParsedImport.Valid
        assertEquals(
            listOf(LogDto("Bench press", "60", listOf("8 / 60", "8")), LogDto("Plank", "", emptyList())),
            parsed.days[LocalDate.of(2026, 10, 2)],
        )
        assertEquals(setOf(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 9)), parsed.days.keys)
        assertEquals(LocalDate.of(2026, 10, 9), parsed.latestDate)
    }
}
