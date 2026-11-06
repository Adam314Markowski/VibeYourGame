package com.byd.workout.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class BydColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val ok: Color,
    val danger: Color,
)

val LightBydColors = BydColors(
    bg = Color(0xFFE9EDF2),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF3F5F9),
    ink = Color(0xFF141A24),
    muted = Color(0xFF5D6777),
    line = Color(0xFFD8DEE7),
    accent = Color(0xFF2B4FE0),
    accentSoft = Color(0xFFE4EAFF),
    onAccent = Color(0xFFFFFFFF),
    ok = Color(0xFF1B8656),
    danger = Color(0xFFC23A3A),
)

val DarkBydColors = BydColors(
    bg = Color(0xFF090C11),
    surface = Color(0xFF151A23),
    surface2 = Color(0xFF1C2230),
    ink = Color(0xFFE7EBF2),
    muted = Color(0xFF97A1B2),
    line = Color(0xFF2A3243),
    accent = Color(0xFF8098FF),
    accentSoft = Color(0xFF212C55),
    onAccent = Color(0xFF0B0F18),
    ok = Color(0xFF4CC38A),
    danger = Color(0xFFF07272),
)
