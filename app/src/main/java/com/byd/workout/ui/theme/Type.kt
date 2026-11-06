package com.byd.workout.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.byd.workout.R

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
)

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
)

private const val TABULAR = "tnum"

private fun display(size: Float, weight: FontWeight, spacing: Float = 0f) = TextStyle(
    fontFamily = BarlowCondensed,
    fontSize = size.sp,
    fontWeight = weight,
    letterSpacing = spacing.em,
    fontFeatureSettings = TABULAR,
)

private fun body(size: Float, weight: FontWeight, spacing: Float = 0f) = TextStyle(
    fontFamily = Barlow,
    fontSize = size.sp,
    fontWeight = weight,
    letterSpacing = spacing.em,
    fontFeatureSettings = TABULAR,
)

object BydType {
    val screenTitle = display(26f, FontWeight.Bold)
    val startDay = display(34f, FontWeight.Bold)
    val exerciseName = display(25f, FontWeight.Bold)
    val timerDigits = display(32f, FontWeight.SemiBold)
    val startButton = display(32f, FontWeight.Bold, spacing = 0.12f)
    val sectionLabel = display(15f, FontWeight.Bold, spacing = 0.06f)
    val eyebrow = body(11f, FontWeight.SemiBold, spacing = 0.08f)
    val body = body(14f, FontWeight.Normal)
    val rowName = body(13.5f, FontWeight.SemiBold)
    val small = body(12f, FontWeight.Normal)

    val calendarMonth = display(18f, FontWeight.Bold)
    val dayTitle = display(19f, FontWeight.Bold)
    val sheetTitle = display(22f, FontWeight.Bold)
    val chip = display(15f, FontWeight.SemiBold)
    val button = body(13f, FontWeight.SemiBold)
    val tiny = body(11f, FontWeight.Normal)
    val setChip = body(11.5f, FontWeight.Normal)
    val navLabel = body(11f, FontWeight.SemiBold)
}
