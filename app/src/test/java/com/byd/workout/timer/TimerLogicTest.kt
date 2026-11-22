package com.byd.workout.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerLogicTest {

    @Test
    fun `+1m while full increases total and remaining`() {
        val state = TimerState(totalSec = 120, remainingSec = 120.0, running = false)
        val newState = state.adjust(60, 0L)

        assertEquals(180, newState.totalSec)
        assertEquals(180.0, newState.remainingSec, 0.001)
    }

    @Test
    fun `-1m at 60 stays 60`() {
        val state = TimerState(totalSec = 60, remainingSec = 60.0, running = false)
        val newState = state.adjust(-60, 0L)

        assertEquals(60, newState.totalSec)
        assertEquals(60.0, newState.remainingSec, 0.001)
    }

    @Test
    fun `+1m while paused at 0-30 of 2-00 increases total and remaining by 1m`() {
        val state = TimerState(totalSec = 120, remainingSec = 30.0, running = false)
        val newState = state.adjust(60, 0L)

        assertEquals(180, newState.totalSec)
        assertEquals(90.0, newState.remainingSec, 0.001)
    }
}
