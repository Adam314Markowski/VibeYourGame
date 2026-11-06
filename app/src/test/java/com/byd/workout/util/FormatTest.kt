package com.byd.workout.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun weightLabel_addsKg() = assertEquals("60 kg", weightLabel("60"))

    @Test
    fun weightLabel_keepsKgSuffix() = assertEquals("60kg", weightLabel("60kg"))

    @Test
    fun weightLabel_keepsUppercaseKg() = assertEquals("60 KG", weightLabel("60 KG"))

    @Test
    fun formatTimer_formatsMinutesAndSeconds() {
        assertEquals("2:00", formatTimer(120.0))
        assertEquals("0:07", formatTimer(7.0))
        assertEquals("0:00", formatTimer(0.0))
    }

    @Test
    fun formatTimer_roundsUp() {
        assertEquals("0:07", formatTimer(6.2))
        assertEquals("1:00", formatTimer(59.01))
    }

    @Test
    fun bydHash_matchesPrototype() {
        assertEquals(((7u * 31u + 97u) * 31u + 98u), bydHash("ab"))
        assertEquals(7u, bydHash(""))
    }
}
