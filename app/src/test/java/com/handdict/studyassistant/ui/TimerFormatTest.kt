package com.handdict.studyassistant.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormatTest {
    @Test
    fun timerTextKeepsFiveCharactersAcrossDisplayRange() {
        listOf(0, 9, 59, 60, 599, 3_599, 5_999).forEach { seconds ->
            assertEquals(5, formatTimer(seconds).length)
        }
        assertEquals("00:00", formatTimer(0))
        assertEquals("99:59", formatTimer(5_999))
    }

    @Test
    fun timerDisplayCapsAtNinetyNineMinutes() {
        assertEquals("99:59", formatTimer(6_000))
    }
}
