package com.handdict.studyassistant.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerChartScaleTest {
    @Test
    fun keepsDefaultScaleThroughTwoHours() {
        assertEquals(120, timerChartAxisMaximum(0))
        assertEquals(120, timerChartAxisMaximum(120))
    }

    @Test
    fun expandsScaleWithoutClippingLongHomeworkDays() {
        assertEquals(180, timerChartAxisMaximum(121))
        assertEquals(180, timerChartAxisMaximum(180))
        assertEquals(240, timerChartAxisMaximum(181))
        assertEquals(300, timerChartAxisMaximum(241))
        assertEquals(360, timerChartAxisMaximum(301))
    }
}
