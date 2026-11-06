package com.byd.workout.ui.history

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byd.workout.R
import com.byd.workout.ui.bydViewModel
import com.byd.workout.ui.components.EmptyText
import com.byd.workout.ui.components.LocalBydSnackbar
import com.byd.workout.ui.components.LogEntryCard
import com.byd.workout.ui.components.ScreenTitle
import com.byd.workout.ui.components.SmallOutlinedButton
import com.byd.workout.ui.theme.BydSpacing
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import com.byd.workout.util.formatLongDate

private val IMPORT_MIME_TYPES = arrayOf("application/json", "text/plain", "application/octet-stream")

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = bydViewModel { c ->
        HistoryViewModel(c.logRepository, c.sessionRepository, c.appContext)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalBydSnackbar.current
    val colors = BydTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.LaunchShare -> context.startActivity(event.intent)
                is HistoryEvent.Message -> snackbar.show(
                    when (val m = event.message) {
                        HistoryMessage.NotBydFile -> context.getString(R.string.import_not_byd)
                        HistoryMessage.ReadFailed -> context.getString(R.string.import_read_failed)
                        HistoryMessage.NothingNew -> context.getString(R.string.import_nothing_new)
                        is HistoryMessage.Imported -> context.resources.getQuantityString(R.plurals.import_done, m.days, m.days)
                        HistoryMessage.Saved -> context.getString(R.string.save_done)
                        HistoryMessage.SaveFailed -> context.getString(R.string.save_failed)
                    },
                )
            }
        }
    }

    DisposableEffect(viewModel) { onDispose { viewModel.closeShare() } }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.import(uri)
    }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) viewModel.saveToFile(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(BydSpacing.screen),
        verticalArrangement = Arrangement.spacedBy(BydSpacing.gap),
    ) {
        item("header") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ScreenTitle(stringResource(R.string.history_title), Modifier.weight(1f))
                SmallOutlinedButton(stringResource(R.string.history_import), onClick = { importLauncher.launch(IMPORT_MIME_TYPES) })
                SmallOutlinedButton(stringResource(R.string.history_share), onClick = viewModel::openShare, accent = true)
            }
        }
        item("calendar") {
            CalendarCard(
                month = state.shownMonth,
                today = state.today,
                selected = state.selected,
                datesWithLogs = state.datesWithLogs,
                onPrevious = viewModel::showPreviousMonth,
                onNext = viewModel::showNextMonth,
                onToday = viewModel::showToday,
                onSelect = viewModel::select,
            )
        }
        item("day-header") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(formatLongDate(state.selected), style = BydType.dayTitle, color = colors.ink, modifier = Modifier.weight(1f))
                if (state.selectedLogs.isNotEmpty()) {
                    Text(
                        pluralStringResource(R.plurals.exercises, state.selectedLogs.size, state.selectedLogs.size),
                        style = BydType.small,
                        color = colors.muted,
                    )
                }
            }
        }
        if (state.selectedLogs.isEmpty()) {
            item("empty") { EmptyText(stringResource(R.string.history_empty_day)) }
        } else {
            itemsIndexed(state.selectedLogs, key = { index, _ -> "${state.selected}-$index" }) { _, entry ->
                LogEntryCard(entry = entry, background = colors.surface, bordered = true)
            }
        }
    }

    state.share?.let { share ->
        val subject = stringResource(R.string.share_subject)
        ShareSheet(
            state = share,
            onPreset = viewModel::applyPreset,
            onFrom = viewModel::setFrom,
            onTo = viewModel::setTo,
            onCancel = viewModel::closeShare,
            onShare = { viewModel.share(subject) },
            onSaveToFile = { viewModel.exportFileName()?.let(saveLauncher::launch) },
        )
    }
}
