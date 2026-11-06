package com.byd.workout.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

val LocalBydColors = staticCompositionLocalOf { LightBydColors }

object BydShapes {
    val card = RoundedCornerShape(16.dp)
    val cardLarge = RoundedCornerShape(18.dp)
    val row = RoundedCornerShape(10.dp)
    val button = RoundedCornerShape(12.dp)
    val startButton = RoundedCornerShape(20.dp)
    val small = RoundedCornerShape(8.dp)
    val setChip = RoundedCornerShape(6.dp)
    val pill = RoundedCornerShape(percent = 50)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}

object BydSpacing {
    val screen = 16.dp
    val gap = 8.dp
}

object BydTheme {
    val colors: BydColors
        @Composable @ReadOnlyComposable get() = LocalBydColors.current
}

@Composable
fun BydTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkBydColors else LightBydColors
    val scheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.accent, onPrimary = colors.onAccent,
        primaryContainer = colors.accentSoft, onPrimaryContainer = colors.ink,
        secondaryContainer = colors.accentSoft, onSecondaryContainer = colors.ink,
        background = colors.bg, onBackground = colors.ink,
        surface = colors.surface, onSurface = colors.ink,
        surfaceVariant = colors.surface2, onSurfaceVariant = colors.muted,
        surfaceContainer = colors.surface, surfaceContainerLow = colors.surface,
        surfaceContainerHigh = colors.surface, surfaceContainerHighest = colors.surface2,
        outline = colors.line, outlineVariant = colors.line,
        error = colors.danger,
    )
    val typography = Typography(
        bodyLarge = BydType.body,
        bodyMedium = BydType.body,
        bodySmall = BydType.small,
        labelLarge = BydType.button,
        labelMedium = BydType.navLabel,
        labelSmall = BydType.tiny,
        titleLarge = BydType.screenTitle,
        titleMedium = BydType.calendarMonth,
        headlineSmall = BydType.sheetTitle,
    )
    CompositionLocalProvider(LocalBydColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            shapes = Shapes(small = BydShapes.small, medium = BydShapes.button, large = BydShapes.card),
            content = content,
        )
    }
}
