package com.byd.workout.ui.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byd.workout.data.LogDto
import com.byd.workout.data.LogRepository
import com.byd.workout.data.SessionRepository
import com.byd.workout.data.export.LogExport
import com.byd.workout.data.export.LogExportFiles
import com.byd.workout.data.export.ParsedImport
import com.byd.workout.data.export.RangeSummary
import com.byd.workout.data.export.SharePreset
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ShareSheetState(
    val preset: SharePreset?,
    val from: LocalDate?,
    val to: LocalDate?,
    val summary: RangeSummary,
)

data class HistoryUiState(
    val today: LocalDate,
    val shownMonth: YearMonth,
    val selected: LocalDate,
    val datesWithLogs: Set<LocalDate>,
    val selectedLogs: List<LogDto>,
    val share: ShareSheetState?,
)

sealed interface HistoryMessage {
    data object NotBydFile : HistoryMessage
    data object ReadFailed : HistoryMessage
    data object NothingNew : HistoryMessage
    data class Imported(val days: Int) : HistoryMessage
    data object Saved : HistoryMessage
    data object SaveFailed : HistoryMessage
}

private data class ShareRange(val preset: SharePreset?, val from: LocalDate?, val to: LocalDate?)

sealed interface HistoryEvent {
    data class Message(val message: HistoryMessage) : HistoryEvent
    data class LaunchShare(val intent: Intent) : HistoryEvent
}

class HistoryViewModel(
    private val logRepository: LogRepository,
    sessionRepository: SessionRepository,
    private val appContext: Context,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {
    private val shownMonth = MutableStateFlow(YearMonth.from(today))
    private val selected = MutableStateFlow(today)
    private val shareRange = MutableStateFlow<ShareRange?>(null)

    private val _events = Channel<HistoryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val allLogs: StateFlow<Map<LocalDate, List<LogDto>>> =
        combine(logRepository.observeAll(), sessionRepository.state) { saved, session ->
            withSessionLogs(saved, session.logs, today)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val uiState: StateFlow<HistoryUiState> =
        combine(shownMonth, selected, allLogs, shareRange) { month, sel, logs, share ->
            HistoryUiState(
                today = today,
                shownMonth = month,
                selected = sel,
                datesWithLogs = logs.filterValues { it.isNotEmpty() }.keys,
                selectedLogs = logs[sel].orEmpty(),
                share = share?.let { ShareSheetState(it.preset, it.from, it.to, LogExport.summarize(logs, it.from, it.to)) },
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HistoryUiState(today, YearMonth.from(today), today, emptySet(), emptyList(), null),
        )

    fun showPreviousMonth() = shownMonth.update { it.minusMonths(1) }

    fun showNextMonth() = shownMonth.update { it.plusMonths(1) }

    fun showToday() {
        shownMonth.value = YearMonth.from(today)
        selected.value = today
    }

    fun select(date: LocalDate) {
        selected.value = date
        shownMonth.value = YearMonth.from(date)
    }

    fun openShare() = applyPreset(SharePreset.ThisMonth)

    fun closeShare() {
        shareRange.value = null
    }

    fun applyPreset(preset: SharePreset) {
        val earliest = allLogs.value.filterValues { it.isNotEmpty() }.keys.minOrNull()
        val (from, to) = LogExport.presetRange(preset, today, earliest)
        shareRange.value = ShareRange(preset, from, to)
    }

    fun setFrom(date: LocalDate) = shareRange.update { ShareRange(null, date, it?.to) }

    fun setTo(date: LocalDate) = shareRange.update { ShareRange(null, it?.from, date) }

    fun exportFileName(): String? = exportRange()?.let { (from, to) -> LogExport.exportFileName(from, to) }

    private fun exportRange(): Pair<LocalDate, LocalDate>? {
        val range = shareRange.value ?: return null
        val from = range.from ?: return null
        val to = range.to ?: return null
        if (LogExport.summarize(allLogs.value, from, to) !is RangeSummary.Counts) return null
        return from to to
    }

    fun share(subject: String) {
        val (from, to) = exportRange() ?: return
        val logs = allLogs.value
        viewModelScope.launch {
            val json = LogExport.buildExportJson(logs, from, to)
            val uri = LogExportFiles.writeExport(appContext, LogExport.exportFileName(from, to), json)
            _events.send(HistoryEvent.LaunchShare(LogExportFiles.shareIntent(uri, subject)))
            closeShare()
        }
    }

    fun saveToFile(uri: Uri) {
        val (from, to) = exportRange() ?: return
        val json = LogExport.buildExportJson(allLogs.value, from, to)
        viewModelScope.launch {
            val message = try {
                LogExportFiles.writeToDocument(appContext, uri, json)
                closeShare()
                HistoryMessage.Saved
            } catch (_: Exception) {
                HistoryMessage.SaveFailed
            }
            _events.send(HistoryEvent.Message(message))
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            val text = try {
                LogExportFiles.readText(appContext, uri)
            } catch (_: Exception) {
                _events.send(HistoryEvent.Message(HistoryMessage.ReadFailed))
                return@launch
            }
            when (val parsed = LogExport.parseImport(text)) {
                ParsedImport.NotBydExport -> _events.send(HistoryEvent.Message(HistoryMessage.NotBydFile))
                is ParsedImport.Valid -> {
                    val added = logRepository.import(parsed)
                    parsed.latestDate?.let(::select)
                    val message = if (added > 0) HistoryMessage.Imported(added) else HistoryMessage.NothingNew
                    _events.send(HistoryEvent.Message(message))
                }
            }
        }
    }

    companion object {
        fun withSessionLogs(saved: Map<LocalDate, List<LogDto>>, sessionLogs: List<LogDto>, today: LocalDate) =
            if (sessionLogs.isEmpty()) saved else saved + (today to saved[today].orEmpty() + sessionLogs)
    }
}
