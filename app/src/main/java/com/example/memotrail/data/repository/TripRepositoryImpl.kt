package com.example.memotrail.data.repository

import com.example.memotrail.data.local.dao.MediaEntryDao
import com.example.memotrail.data.local.dao.TripDao
import com.example.memotrail.data.local.dao.TripDayDao
import com.example.memotrail.data.local.dao.TripTagDao
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.local.relation.TripDayWithMedia
import com.example.memotrail.data.local.relation.TripWithDays
import com.example.memotrail.data.model.MediaType
import kotlinx.coroutines.flow.Flow

class TripRepositoryImpl(
    private val tripDao: TripDao,
    private val tripDayDao: TripDayDao,
    private val mediaEntryDao: MediaEntryDao,
    private val tripTagDao: TripTagDao
) : TripRepository {

    private fun resolveUpsertId(entityId: Long, upsertResultId: Long): Long {
        return if (entityId > 0L) entityId else upsertResultId
    }

    override fun observeTrips(): Flow<List<TripEntity>> =
        tripDao.observeTripsByNewestStartDate()

    override fun searchTrips(query: String): Flow<List<TripEntity>> =
        tripDao.observeTripsBySearch(query)

    override fun observeTrip(tripId: Long): Flow<TripEntity?> =
        tripDao.observeTripById(tripId)

    override fun observeTripWithDays(tripId: Long): Flow<TripWithDays?> =
        tripDao.observeTripWithDays(tripId)

    override fun observeTripTagNames(tripId: Long): Flow<List<String>> =
        tripTagDao.observeTagNamesForTrip(tripId)

    override fun observeDaysForTrip(tripId: Long): Flow<List<TripDayEntity>> =
        tripDayDao.observeDaysForTrip(tripId)

    override fun observeDayWithMedia(dayId: Long): Flow<TripDayWithMedia?> =
        tripDayDao.observeDayWithMedia(dayId)

    override fun observeMediaForDay(dayId: Long): Flow<List<MediaEntryEntity>> =
        mediaEntryDao.observeMediaForDay(dayId)

    override fun observeMediaForDayByType(dayId: Long, type: MediaType): Flow<List<MediaEntryEntity>> =
        mediaEntryDao.observeMediaForDayByType(dayId, type)

    override suspend fun upsertTrip(trip: TripEntity): Long {
        val upsertResultId = tripDao.upsertTrip(trip)
        return resolveUpsertId(trip.id, upsertResultId)
    }

    override suspend fun upsertTripWithTags(trip: TripEntity, tagNames: List<String>): Long {
        val upsertResultId = tripDao.upsertTrip(trip)
        val tripId = resolveUpsertId(trip.id, upsertResultId)
        tripTagDao.replaceTripTags(tripId = tripId, tagNames = tagNames)
        return tripId
    }

    override suspend fun replaceTripTags(tripId: Long, tagNames: List<String>) {
        tripTagDao.replaceTripTags(tripId = tripId, tagNames = tagNames)
    }

    override suspend fun deleteTrip(trip: TripEntity) {
        tripDao.deleteTrip(trip)
    }

    override suspend fun upsertDay(day: TripDayEntity): Long {
        val upsertResultId = tripDayDao.upsertDay(day)
        return resolveUpsertId(day.id, upsertResultId)
    }

    override suspend fun deleteDay(day: TripDayEntity) {
        tripDayDao.deleteDay(day)
    }

    override suspend fun upsertMedia(media: MediaEntryEntity): Long {
        val upsertResultId = mediaEntryDao.upsertMedia(media)
        return resolveUpsertId(media.id, upsertResultId)
    }

    override suspend fun deleteMedia(media: MediaEntryEntity) {
        mediaEntryDao.deleteMedia(media)
    }
}
