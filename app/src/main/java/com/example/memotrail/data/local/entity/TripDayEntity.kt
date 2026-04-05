package com.example.memotrail.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_days",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["dayDateEpochDay"]),
        Index(value = ["locationName"])
    ]
)
data class TripDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val dayDateEpochDay: Long,
    val locationName: String,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val notes: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
