package com.example.memotrail.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity

data class TripDayWithMedia(
    @Embedded
    val day: TripDayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripDayId"
    )
    val media: List<MediaEntryEntity>
)

