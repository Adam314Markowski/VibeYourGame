package com.byd.workout.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import java.util.Locale

fun dayIndexOf(date: LocalDate): Int = date.dayOfWeek.value - 1

fun todayIndex(): Int = dayIndexOf(LocalDate.now())

fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

private val isoDatePattern = Regex("""^\d{4}-\d{2}-\d{2}$""")

fun parseIsoDateOrNull(text: String): LocalDate? {
    if (!isoDatePattern.matches(text)) return null
    return try {
        LocalDate.parse(text)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun calendarGrid(month: YearMonth): List<LocalDate> {
    val start = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0 until 42).map { start.plusDays(it.toLong()) }
    val sixthRow = days.subList(35, 42)
    return if (sixthRow.all { YearMonth.from(it) != month }) days.subList(0, 35) else days
}

private fun String.capitalizeFirst(locale: Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

fun formatMonthYear(month: YearMonth, locale: Locale = Locale.getDefault()): String =
    month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).capitalizeFirst(locale)

fun formatLongDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale)).capitalizeFirst(locale)

fun formatMediumDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
