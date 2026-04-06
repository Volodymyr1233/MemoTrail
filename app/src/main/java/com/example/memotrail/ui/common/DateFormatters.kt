package com.example.memotrail.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun formatEpochDay(epochDay: Long?): String {
    if (epochDay == null) return "-"
    return LocalDate.ofEpochDay(epochDay).format(dayFormatter)
}

fun formatEpochMillis(epochMillis: Long): String {
    val localDate = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return localDate.format(dayFormatter)
}

fun parseIsoDateToEpochDay(input: String): Long? {
    return runCatching { LocalDate.parse(input.trim()).toEpochDay() }.getOrNull()
}

fun formatEpochDayIso(epochDay: Long?): String {
    if (epochDay == null) return ""
    return LocalDate.ofEpochDay(epochDay).format(isoFormatter)
}


