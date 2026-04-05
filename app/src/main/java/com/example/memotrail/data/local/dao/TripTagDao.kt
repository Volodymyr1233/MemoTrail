package com.example.memotrail.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.memotrail.data.local.entity.TagEntity
import com.example.memotrail.data.local.entity.TripTagCrossRefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripTagDao {

    @Query(
        """
        SELECT t.name
        FROM tags t
        INNER JOIN trip_tag_cross_ref x ON x.tagId = t.id
        WHERE x.tripId = :tripId
        ORDER BY t.name COLLATE NOCASE ASC
        """
    )
    fun observeTagNamesForTrip(tripId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT id FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTagIdByName(name: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: TripTagCrossRefEntity)

    @Query("DELETE FROM trip_tag_cross_ref WHERE tripId = :tripId")
    suspend fun deleteCrossRefsForTrip(tripId: Long)

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tagId FROM trip_tag_cross_ref)")
    suspend fun deleteOrphanTags()

    @Transaction
    suspend fun replaceTripTags(tripId: Long, tagNames: List<String>) {
        val cleaned = tagNames
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .toList()

        deleteCrossRefsForTrip(tripId)

        for (name in cleaned) {
            insertTag(TagEntity(name = name))
            val tagId = getTagIdByName(name) ?: continue
            insertCrossRef(TripTagCrossRefEntity(tripId = tripId, tagId = tagId))
        }

        deleteOrphanTags()
    }
}

