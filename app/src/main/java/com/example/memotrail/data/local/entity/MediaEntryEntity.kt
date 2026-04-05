package com.example.memotrail.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.memotrail.data.model.MediaType

@Entity(
    tableName = "media_entries",
    foreignKeys = [
        ForeignKey(
            entity = TripDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripDayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tripDayId"]),
        Index(value = ["type"])
    ]
)
data class MediaEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripDayId: Long,
    val type: MediaType,
    val uri: String,
    val thumbnailUri: String? = null,
    val durationMs: Long? = null,
    val caption: String? = null,
    val pinLat: Double? = null,
    val pinLng: Double? = null,
    val createdAtEpochMillis: Long
)
