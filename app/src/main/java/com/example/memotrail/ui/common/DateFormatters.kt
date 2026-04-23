package com.example.memotrail.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun localizedDayFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
}

fun formatEpochDay(epochDay: Long?): String {
    if (epochDay == null) return "-"
    return LocalDate.ofEpochDay(epochDay).format(localizedDayFormatter())
}

fun formatEpochMillis(epochMillis: Long): String {
    val localDate = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return localDate.format(localizedDayFormatter())
}

fun parseIsoDateToEpochDay(input: String): Long? {
    return runCatching { LocalDate.parse(input.trim()).toEpochDay() }.getOrNull()
}

fun formatEpochDayIso(epochDay: Long?): String {
    if (epochDay == null) return ""
    return LocalDate.ofEpochDay(epochDay).format(isoFormatter)
}


