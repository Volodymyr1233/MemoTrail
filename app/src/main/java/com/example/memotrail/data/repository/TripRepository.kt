package com.example.memotrail.data.repository

import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.local.relation.TripDayWithMedia
import com.example.memotrail.data.local.relation.TripWithDays
import com.example.memotrail.data.model.MediaType
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTrips(): Flow<List<TripEntity>>
    fun searchTrips(query: String): Flow<List<TripEntity>>
    fun observeTrip(tripId: Long): Flow<TripEntity?>
    fun observeTripWithDays(tripId: Long): Flow<TripWithDays?>
    fun observeTripTagNames(tripId: Long): Flow<List<String>>
    fun observeDaysForTrip(tripId: Long): Flow<List<TripDayEntity>>
    fun observeDayWithMedia(dayId: Long): Flow<TripDayWithMedia?>
    fun observeMediaForDay(dayId: Long): Flow<List<MediaEntryEntity>>
    fun observeMediaForDayByType(dayId: Long, type: MediaType): Flow<List<MediaEntryEntity>>

    suspend fun upsertTrip(trip: TripEntity): Long
    suspend fun upsertTripWithTags(trip: TripEntity, tagNames: List<String>): Long
    suspend fun replaceTripTags(tripId: Long, tagNames: List<String>)
    suspend fun deleteTrip(trip: TripEntity)
    suspend fun upsertDay(day: TripDayEntity): Long
    suspend fun deleteDay(day: TripDayEntity)
    suspend fun upsertMedia(media: MediaEntryEntity): Long
    suspend fun deleteMedia(media: MediaEntryEntity)
}
