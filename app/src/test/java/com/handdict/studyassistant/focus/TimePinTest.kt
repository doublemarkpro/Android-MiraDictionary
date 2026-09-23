package com.handdict.studyassistant.focus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TimePinTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val now = Instant.parse("2026-09-23T12:06:34Z").toEpochMilli()

    @Test
    fun acceptsCurrentHourAndMinute() {
        assertTrue(TimePin.verify("2006", now, zone))
    }

    @Test
    fun acceptsPreviousMinuteAcrossMinuteBoundary() {
        assertTrue(TimePin.verify("2005", now, zone))
    }

    @Test
    fun rejectsOtherValues() {
        assertFalse(TimePin.verify("2004", now, zone))
        assertFalse(TimePin.verify("abcd", now, zone))
    }
}
