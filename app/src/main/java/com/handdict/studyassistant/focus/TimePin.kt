package com.handdict.studyassistant.focus

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimePin {
    private val formatter = DateTimeFormatter.ofPattern("HHmm")

    fun verify(
        pin: String,
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (!Regex("\\d{4}").matches(pin)) return false
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        return pin == now.format(formatter) || pin == now.minusMinutes(1).format(formatter)
    }
}
