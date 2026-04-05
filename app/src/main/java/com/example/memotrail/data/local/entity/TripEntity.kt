package com.example.memotrail.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["title"]),
        Index(value = ["locationName"]),
        Index(value = ["startDateEpochDay"])
    ]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val locationName: String,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val coverImageUri: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
