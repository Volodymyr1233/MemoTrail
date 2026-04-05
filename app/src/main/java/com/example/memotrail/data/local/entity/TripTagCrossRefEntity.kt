package com.example.memotrail.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "trip_tag_cross_ref",
    primaryKeys = ["tripId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["tagId"])
    ]
)
data class TripTagCrossRefEntity(
    val tripId: Long,
    val tagId: Long
)

