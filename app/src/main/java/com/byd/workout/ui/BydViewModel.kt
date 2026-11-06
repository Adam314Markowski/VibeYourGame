package com.byd.workout.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.byd.workout.AppContainer
import com.byd.workout.BydApp

@Composable
inline fun <reified VM : ViewModel> bydViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM = viewModel(
    key = key,
    factory = viewModelFactory {
        initializer { create((this[APPLICATION_KEY] as BydApp).container) }
    },
)
