package com.example.memotrail

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.memotrail.data.local.MemoTrailDatabase
import com.example.memotrail.data.local.dao.TripDao
import com.example.memotrail.data.local.dao.TripDayDao
import com.example.memotrail.data.local.dao.MediaEntryDao
import com.example.memotrail.data.local.dao.TripTagDao
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.model.MediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TripDatabaseTest {

    private lateinit var tripDao: TripDao
    private lateinit var tripDayDao: TripDayDao
    private lateinit var mediaEntryDao: MediaEntryDao
    private lateinit var db: MemoTrailDatabase

    private lateinit var tripTagDao: TripTagDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, MemoTrailDatabase::class.java
        ).build()
        tripDao = db.tripDao()
        tripDayDao = db.tripDayDao()
        mediaEntryDao = db.mediaEntryDao()
        tripTagDao = db.tripTagDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndObserveTrip() = runBlocking {
        val trip = createTestTrip("Mountains", "Poland")
        tripDao.upsertTrip(trip)
        val allTrips = tripDao.observeTripsByNewestStartDate().first()

        assertEquals(1, allTrips.size)
        assertEquals("Mountains", allTrips[0].title)
    }

    @Test
    fun deleteTripTest() = runBlocking {
        val trip = createTestTrip("Sea", "Italy")
        val id = tripDao.upsertTrip(trip)
        val tripWithId = trip.copy(id = id)

        tripDao.deleteTrip(tripWithId)
        val allTrips = tripDao.observeTripsByNewestStartDate().first()
        assertEquals(0, allTrips.size)
    }

    @Test
    fun searchTripTest() = runBlocking {
        tripDao.upsertTrip(createTestTrip("Hapka", "Italy"))
        tripDao.upsertTrip(createTestTrip("Work", "Germany"))
        val searchResult = tripDao.observeTripsBySearch("Italy").first()

        assertEquals(1, searchResult.size)
        assertEquals("Hapka", searchResult[0].title)
    }

    private fun createTestTrip(title: String, location: String): TripEntity {
        return TripEntity(id = 0, title = title, locationName = location, startDateEpochDay = 19000, endDateEpochDay = 19010,
            coverImageUri = null, createdAtEpochMillis = System.currentTimeMillis(), updatedAtEpochMillis = System.currentTimeMillis()
        )
    }

    @Test
    fun insertAndObserveDayTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("France", "Nitca"))

        val day = TripDayEntity(
            id = 0, tripId = tripId, dayDateEpochDay = 20000, locationName = "Beach",
            notes = "hapka hapka hapka", createdAtEpochMillis = System.currentTimeMillis(), updatedAtEpochMillis = System.currentTimeMillis()
        )
        tripDayDao.upsertDay(day)
        val days = tripDayDao.observeDaysForTrip(tripId).first()

        assertEquals(1, days.size)
        assertEquals("Beach", days[0].locationName)
    }

    @Test
    fun observeTripWithDaysTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("Germany", "Munich"))
        tripDayDao.upsertDay(createTestDay(tripId, "Uni"))
        tripDayDao.upsertDay(createTestDay(tripId, "CJVM"))
        val tripWithDays = tripDao.observeTripWithDays(tripId).first()

        assertEquals("Germany", tripWithDays?.trip?.title)
        assertEquals(2, tripWithDays?.days?.size)
    }

    @Test
    fun cascadeDeleteTest() = runBlocking {
        val trip = createTestTrip("Delitik", "delitka")
        val tripId = tripDao.upsertTrip(trip)
        tripDayDao.upsertDay(createTestDay(tripId, "delitki"))
        tripDao.deleteTrip(trip.copy(id = tripId))

        val daysAfterDelete = tripDayDao.observeDaysForTrip(tripId).first()
        assertEquals(0, daysAfterDelete.size)
    }

    private fun createTestDay(tripId: Long, location: String): TripDayEntity {
        return TripDayEntity(id = 0, tripId = tripId, dayDateEpochDay = 20000, locationName = location,
            notes = "Note", createdAtEpochMillis = System.currentTimeMillis(), updatedAtEpochMillis = System.currentTimeMillis()
        )
    }

    @Test
    fun insertAndObserveMediaTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("Italy", "Rome"))
        val dayId = tripDayDao.upsertDay(createTestDay(tripId, "Gelato"))

        val media = MediaEntryEntity(id = 0, tripDayId = dayId, type = MediaType.IMAGE,
            uri = "content://media/external/images/1", createdAtEpochMillis = System.currentTimeMillis())
        mediaEntryDao.upsertMedia(media)

        val mediaList = mediaEntryDao.observeMediaForDay(dayId).first()
        assertEquals(1, mediaList.size)
        assertEquals(MediaType.IMAGE, mediaList[0].type)
    }

    @Test
    fun observeMediaByTypeTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("Dogs dogs", "Tokio"))
        val dayId = tripDayDao.upsertDay(createTestDay(tripId, "doooooooogs"))

        mediaEntryDao.upsertMedia(createTestMedia(dayId, MediaType.IMAGE, "uri_photo"))
        mediaEntryDao.upsertMedia(createTestMedia(dayId, MediaType.VIDEO, "uri_video"))

        val photosOnly = mediaEntryDao.observeMediaForDayByType(dayId, MediaType.IMAGE).first()
        assertEquals(1, photosOnly.size)
        assertEquals(MediaType.IMAGE, photosOnly[0].type)
    }

    @Test
    fun observeDayWithMediaTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("Best best ever", "Barselona"))
        val dayId = tripDayDao.upsertDay(createTestDay(tripId, "Pepik"))

        mediaEntryDao.upsertMedia(createTestMedia(dayId, MediaType.IMAGE, "photo_1"))
        mediaEntryDao.upsertMedia(createTestMedia(dayId, MediaType.IMAGE, "photo_2"))

        val dayWithMedia = tripDayDao.observeDayWithMedia(dayId).first()

        assertEquals(2, dayWithMedia?.media?.size)
        assertEquals("Pepik", dayWithMedia?.day?.locationName)
    }

    private fun createTestMedia(dayId: Long, type: MediaType, uri: String): MediaEntryEntity {
        return MediaEntryEntity(id = 0, tripDayId = dayId, type = type,
            uri = uri, createdAtEpochMillis = System.currentTimeMillis()
        )
    }

    @Test
    fun replaceAndObserveTagsTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("Norway", "Oslo"))
        val tags = listOf("Nature", "Cold", " Fiords")
        tripTagDao.replaceTripTags(tripId, tags)
        val savedTags = tripTagDao.observeTagNamesForTrip(tripId).first()

        assertEquals(3, savedTags.size)
        assert(savedTags.contains("Fiords"))
    }

    @Test
    fun searchByTagTest() = runBlocking {
        val tripId1 = tripDao.upsertTrip(createTestTrip("Happy 1", "Spepka 1"))
        val tripId2 = tripDao.upsertTrip(createTestTrip("Happy 2", "Spepka 2"))
        tripTagDao.replaceTripTags(tripId1, listOf("Sea"))
        tripTagDao.replaceTripTags(tripId2, listOf("Mountains"))

        val result = tripDao.observeTripsBySearch("Sea").first()
        assertEquals(1, result.size)
        assertEquals("Happy 1", result[0].title)
    }

    @Test
    fun deleteOrphanTagsTest() = runBlocking {
        val tripId = tripDao.upsertTrip(createTestTrip("More dogs", "Paris"))
        tripTagDao.replaceTripTags(tripId, listOf("Deletic"))
        tripTagDao.replaceTripTags(tripId, emptyList())
        val tagId = tripTagDao.getTagIdByName("Deletic")
        assertNull("Mew mew mew mew mew", tagId)
    }
}