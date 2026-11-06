package com.byd.workout.data

import kotlinx.serialization.Serializable

@Serializable
data class SessionState(
    val active: Boolean = false,
    val pickDay: Int? = null,
    val current: String? = null,
    val weight: String = "",
    val sets: List<String> = listOf(""),
    val currentSet: Int = 0,
    val upcoming: List<String> = emptyList(),
    val logs: List<LogDto> = emptyList(),
)

@Serializable
data class LogDto(val name: String, val weight: String, val sets: List<String>)
