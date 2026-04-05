package com.example.memotrail.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaEntryDao {

    @Query("SELECT * FROM media_entries WHERE tripDayId = :dayId ORDER BY createdAtEpochMillis ASC")
    fun observeMediaForDay(dayId: Long): Flow<List<MediaEntryEntity>>

    @Query(
        """
        SELECT * FROM media_entries
        WHERE tripDayId = :dayId AND type = :type
        ORDER BY createdAtEpochMillis ASC
        """
    )
    fun observeMediaForDayByType(dayId: Long, type: MediaType): Flow<List<MediaEntryEntity>>

    @Upsert
    suspend fun upsertMedia(media: MediaEntryEntity): Long

    @Delete
    suspend fun deleteMedia(media: MediaEntryEntity)
}

