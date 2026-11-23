package com.byd.workout.timer

import kotlin.math.max

data class TimerState(
    val totalSec: Int = 120,
    val remainingSec: Double = 120.0,
    val running: Boolean = false,
    val endAtElapsed: Long = 0,
)

fun TimerState.start(now: Long): TimerState {
    val remainingToStart = if (remainingSec <= 0) totalSec.toDouble() else remainingSec
    return copy(
        remainingSec = remainingToStart,
        running = true,
        endAtElapsed = now + (remainingToStart * 1000).toLong()
    )
}

fun TimerState.pause(now: Long): TimerState {
    if (!running) return this
    return copy(
        remainingSec = remainingAt(now),
        running = false
    )
}

fun TimerState.reset(): TimerState {
    return copy(
        remainingSec = totalSec.toDouble(),
        running = false
    )
}

fun TimerState.adjust(deltaSec: Int, now: Long): TimerState {
    val currentRemaining = if (running) remainingAt(now) else remainingSec
    val wasFull = currentRemaining >= totalSec

    val newTotal = max(60, totalSec + deltaSec)
    val newRemaining = if (wasFull) {
        newTotal.toDouble()
    } else {
        (currentRemaining + deltaSec).coerceIn(0.0, newTotal.toDouble())
    }

    return if (running) {
        copy(
            totalSec = newTotal,
            remainingSec = newRemaining,
            endAtElapsed = now + (newRemaining * 1000).toLong()
        )
    } else {
        copy(
            totalSec = newTotal,
            remainingSec = newRemaining
        )
    }
}

fun TimerState.remainingAt(now: Long): Double {
    if (!running) return remainingSec
    val diffMs = endAtElapsed - now
    return max(0.0, diffMs / 1000.0)
}
