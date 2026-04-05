package com.example.memotrail.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.local.relation.TripWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY startDateEpochDay DESC")
    fun observeTripsByNewestStartDate(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun observeTripById(tripId: Long): Flow<TripEntity?>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun observeTripWithDays(tripId: Long): Flow<TripWithDays?>

    @Query(
        """
        SELECT DISTINCT t.*
        FROM trips t
        LEFT JOIN trip_tag_cross_ref x ON x.tripId = t.id
        LEFT JOIN tags g ON g.id = x.tagId
        WHERE t.title LIKE '%' || :query || '%'
           OR t.locationName LIKE '%' || :query || '%'
           OR g.name LIKE '%' || :query || '%'
        ORDER BY t.startDateEpochDay DESC
        """
    )
    fun observeTripsBySearch(query: String): Flow<List<TripEntity>>

    @Upsert
    suspend fun upsertTrip(trip: TripEntity): Long

    @Delete
    suspend fun deleteTrip(trip: TripEntity)
}
