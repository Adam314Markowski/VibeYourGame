package com.byd.workout.ui.exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.byd.workout.R
import com.byd.workout.timer.TimerPanel
import com.byd.workout.ui.components.ScreenTitle
import com.byd.workout.ui.theme.BydSpacing

@Composable
fun ExerciseScreen() {
    Box(Modifier.fillMaxSize()) {
        ScreenTitle(
            text = stringResource(R.string.tab_exercise),
            modifier = Modifier.padding(BydSpacing.screen)
        )
        TimerPanel(Modifier.align(Alignment.Center))
    }
}
