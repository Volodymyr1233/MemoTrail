package com.example.memotrail.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.relation.TripDayWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDayDao {

    @Query("SELECT * FROM trip_days WHERE tripId = :tripId ORDER BY dayDateEpochDay ASC")
    fun observeDaysForTrip(tripId: Long): Flow<List<TripDayEntity>>

    @Transaction
    @Query("SELECT * FROM trip_days WHERE id = :dayId LIMIT 1")
    fun observeDayWithMedia(dayId: Long): Flow<TripDayWithMedia?>

    @Upsert
    suspend fun upsertDay(day: TripDayEntity): Long

    @Delete
    suspend fun deleteDay(day: TripDayEntity)
}

