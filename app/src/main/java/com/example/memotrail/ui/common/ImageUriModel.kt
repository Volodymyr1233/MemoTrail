package com.example.memotrail.ui.common

import android.net.Uri
import java.io.File

fun imageModelFromStoredUri(storedUri: String?): Any? {
    val value = storedUri?.trim().orEmpty()
    if (value.isBlank()) return null

    val parsed = Uri.parse(value)
    return if (parsed.scheme.isNullOrBlank()) {
        Uri.fromFile(File(value))
    } else {
        parsed
    }
}

