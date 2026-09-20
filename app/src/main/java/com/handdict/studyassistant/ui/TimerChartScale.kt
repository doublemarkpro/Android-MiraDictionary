package com.handdict.studyassistant.ui

internal fun timerChartAxisMaximum(maximumMinutes: Int): Int {
    val safeMaximum = maximumMinutes.coerceAtLeast(0)
    return when {
        safeMaximum <= 120 -> 120
        safeMaximum <= 180 -> 180
        safeMaximum <= 240 -> 240
        else -> ((safeMaximum + 59) / 60) * 60
    }
}
