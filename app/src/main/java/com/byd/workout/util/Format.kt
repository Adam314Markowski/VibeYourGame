package com.byd.workout.util

import kotlin.math.ceil

fun weightLabel(weight: String): String =
    if (weight.trimEnd().endsWith("kg", ignoreCase = true)) weight else "$weight kg"

fun formatTimer(seconds: Double): String {
    val whole = ceil(seconds).toLong().coerceAtLeast(0)
    return "%d:%02d".format(whole / 60, whole % 60)
}

fun bydHash(text: String): UInt {
    var hash = 7u
    for (char in text) hash = hash * 31u + char.code.toUInt()
    return hash
}
