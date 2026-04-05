package com.example.memotrail.data.local

import androidx.room.TypeConverter
import com.example.memotrail.data.model.MediaType

class Converters {

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
}

