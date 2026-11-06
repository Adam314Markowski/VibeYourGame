package com.byd.workout.ui.history

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.workout.R
import com.byd.workout.data.export.RangeSummary
import com.byd.workout.data.export.SharePreset
import com.byd.workout.ui.components.Eyebrow
import com.byd.workout.ui.theme.BydShapes
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import com.byd.workout.util.formatMediumDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@StringRes
private fun SharePreset.label(): Int = when (this) {
    SharePreset.ThisWeek -> R.string.share_preset_week
    SharePreset.ThisMonth -> R.string.share_preset_month
    SharePreset.Last30Days -> R.string.share_preset_30
    SharePreset.AllTime -> R.string.share_preset_all
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShareSheet(
    state: ShareSheetState,
    onPreset: (SharePreset) -> Unit,
    onFrom: (LocalDate) -> Unit,
    onTo: (LocalDate) -> Unit,
    onCancel: () -> Unit,
    onShare: () -> Unit,
    onSaveToFile: () -> Unit,
) {
    val colors = BydTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        shape = BydShapes.sheet,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.share_title), style = BydType.sheetTitle, color = colors.ink)
                Text(stringResource(R.string.share_subtitle), style = BydType.body.copy(fontSize = 13.sp), color = colors.muted)
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SharePreset.entries.forEach { preset ->
                    PresetChip(text = stringResource(preset.label()), selected = state.preset == preset, onClick = { onPreset(preset) })
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField(stringResource(R.string.share_from), state.from, onFrom, Modifier.weight(1f))
                DateField(stringResource(R.string.share_to), state.to, onTo, Modifier.weight(1f))
            }

            Text(text = summaryText(state.summary), style = BydType.body.copy(fontSize = 13.sp), color = colors.ink)

            val canExport = state.summary is RangeSummary.Counts
            OutlinedButton(
                onClick = onSaveToFile,
                enabled = canExport,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = BydShapes.button,
                border = BorderStroke(1.dp, if (canExport) colors.accent else colors.line),
            ) {
                Text(
                    stringResource(R.string.action_save_to_file),
                    style = BydType.button,
                    color = if (canExport) colors.accent else colors.muted,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = BydShapes.button,
                    border = BorderStroke(1.dp, colors.line),
                ) { Text(stringResource(R.string.action_cancel), style = BydType.button, color = colors.ink) }
                Button(
                    onClick = onShare,
                    enabled = canExport,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = BydShapes.button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent,
                        disabledContainerColor = colors.accent.copy(alpha = 0.4f),
                        disabledContentColor = colors.onAccent,
                    ),
                ) { Text(stringResource(R.string.action_share), style = BydType.button) }
            }
        }
    }
}

@Composable
private fun summaryText(summary: RangeSummary): String = when (summary) {
    RangeSummary.InvalidRange -> stringResource(R.string.share_summary_invalid)
    RangeSummary.Empty -> stringResource(R.string.share_summary_empty)
    is RangeSummary.Counts -> stringResource(
        R.string.share_summary_counts,
        pluralStringResource(R.plurals.training_days, summary.days, summary.days),
        pluralStringResource(R.plurals.exercises, summary.exercises, summary.exercises),
    )
}

@Composable
private fun PresetChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = BydTheme.colors
    val shape = BydShapes.pill
    Text(
        text = text,
        style = BydType.button,
        color = if (selected) colors.accent else colors.ink,
        modifier = Modifier
            .background(if (selected) colors.accentSoft else colors.surface2, shape)
            .border(1.dp, if (selected) colors.accent else colors.line, shape)
            .semantics { this.selected = selected }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate?, onPick: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val colors = BydTheme.colors
    var open by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Eyebrow(label)
        Text(
            text = date?.let { formatMediumDate(it) } ?: "—",
            style = BydType.body,
            color = colors.ink,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface2, BydShapes.small)
                .border(1.dp, colors.line, BydShapes.small)
                .clickable(role = Role.Button) { open = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
    if (open) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date?.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onPick(it.toUtcLocalDate()) }
                    open = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
