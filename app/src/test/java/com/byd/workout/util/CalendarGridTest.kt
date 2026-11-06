package com.byd.workout.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarGridTest {
    @Test
    fun october2026_startsOnMonday28September_andHas5Rows() {
        val grid = calendarGrid(YearMonth.of(2026, 10))
        assertEquals(LocalDate.of(2026, 9, 28), grid.first())
        assertEquals(5, grid.size / 7)
        assertEquals(LocalDate.of(2026, 11, 1), grid.last())
    }

    @Test
    fun monthNeedingSixRows_keepsSixthRow() {
        val grid = calendarGrid(YearMonth.of(2026, 3))
        assertEquals(6, grid.size / 7)
        assertEquals(LocalDate.of(2026, 2, 23), grid.first())
    }

    @Test
    fun sixthRowEntirelyInNextMonth_isDropped() {
        val grid = calendarGrid(YearMonth.of(2027, 2))
        assertEquals(5, grid.size / 7)
    }

    @Test
    fun gridAlwaysStartsOnMonday() {
        for (month in 1..12) {
            assertEquals(DayOfWeek.MONDAY, calendarGrid(YearMonth.of(2026, month)).first().dayOfWeek)
        }
    }

    @Test
    fun dayIndex_isMondayFirst() {
        assertEquals(0, dayIndexOf(LocalDate.of(2026, 10, 19)))
        assertEquals(6, dayIndexOf(LocalDate.of(2026, 10, 25)))
    }

    @Test
    fun parseIsoDate_rejectsBadInput() {
        assertEquals(LocalDate.of(2026, 10, 2), parseIsoDateOrNull("2026-10-02"))
        assertEquals(null, parseIsoDateOrNull("2026-13-02"))
        assertEquals(null, parseIsoDateOrNull("2026-1-2"))
        assertEquals(null, parseIsoDateOrNull("yesterday"))
    }
}
