package com.byd.workout.ui.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byd.workout.R
import com.byd.workout.ui.components.PlaceholderScreen

@Composable
fun PlanScreen(onEditDay: (dayIndex: Int) -> Unit) {
    PlaceholderScreen(stringResource(R.string.tab_plan))
}

@Composable
fun PlanEditScreen(dayIndex: Int, onClose: () -> Unit) {
    PlaceholderScreen(stringResource(R.string.tab_plan))
}
