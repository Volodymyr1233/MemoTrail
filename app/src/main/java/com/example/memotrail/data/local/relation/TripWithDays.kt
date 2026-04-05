package com.example.memotrail.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.TripEntity

data class TripWithDays(
    @Embedded
    val trip: TripEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val days: List<TripDayEntity>
)

