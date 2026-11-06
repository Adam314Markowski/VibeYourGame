package com.byd.workout.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.byd.workout.R
import com.byd.workout.ui.components.BydCard
import com.byd.workout.ui.theme.BydShapes
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import com.byd.workout.util.calendarGrid
import com.byd.workout.util.formatLongDate
import com.byd.workout.util.formatMonthYear
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarCard(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    datesWithLogs: Set<LocalDate>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BydTheme.colors
    val days = remember(month) { calendarGrid(month) }
    BydCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = formatMonthYear(month),
                style = BydType.calendarMonth,
                color = colors.ink,
                modifier = Modifier.weight(1f).padding(start = 2.dp),
            )
            IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.history_previous_month),
                    tint = colors.ink,
                )
            }
            TextButton(onClick = onToday, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(stringResource(R.string.history_today), style = BydType.button.copy(fontSize = 12.sp), color = colors.ink)
            }
            IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.history_next_month),
                    tint = colors.ink,
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)) {
            stringArrayResource(R.array.weekday_short).forEach { label ->
                Text(
                    text = label.uppercase(),
                    style = BydType.tiny.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em),
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        inMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        isFuture = date.isAfter(today),
                        hasLog = date in datesWithLogs,
                        isSelected = date == selected,
                        onClick = { onSelect(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    hasLog: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BydTheme.colors
    val shape = BydShapes.row
    val textColor = when {
        isSelected -> colors.onAccent
        !inMonth || isFuture -> colors.muted
        else -> colors.ink
    }
    val label = formatLongDate(date)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(36.dp)
            .alpha(if (!inMonth && !isSelected) 0.45f else 1f)
            .clip(shape)
            .background(if (isSelected) colors.accent else colors.surface, shape)
            .then(if (isToday) Modifier.border(1.5.dp, colors.accent, shape) else Modifier)
            .semantics {
                contentDescription = label
                selected = isSelected
            }
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = BydType.body.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
            color = textColor,
        )
        if (hasLog) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .background(if (isSelected) colors.onAccent else colors.accent, CircleShape),
            )
        }
    }
}
