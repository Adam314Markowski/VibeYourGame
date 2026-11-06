package com.byd.workout.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byd.workout.ui.theme.BydSpacing

@Composable
fun PlaceholderScreen(title: String) {
    ScreenTitle(title, Modifier.fillMaxSize().padding(BydSpacing.screen))
}
