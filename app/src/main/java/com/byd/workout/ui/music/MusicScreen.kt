package com.byd.workout.ui.music

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byd.workout.R
import com.byd.workout.timer.TimerPanel
import com.byd.workout.ui.components.PlaceholderScreen

@Composable
fun MusicScreen() {
    Column {
        PlaceholderScreen(stringResource(R.string.tab_music))
        TimerPanel()
    }
}
