package com.example.memotrail.ui.tripform

import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.repository.TripRepository
import com.example.memotrail.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TripRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `loadForEdit populates uiState from repository`() = runTest(mainDispatcherRule.dispatcher) {
        val trip = makeTrip(id = 12L, title = "Summer", createdAt = 111L)
        every { repository.observeTrip(12L) } returns flowOf(trip)
        every { repository.observeTripTagNames(12L) } returns flowOf(listOf("Beach", "Family"))

        val viewModel = TripFormViewModel(repository)
        viewModel.loadForEdit(12L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals(12L, state.tripId)
        assertEquals("Summer", state.title)
        assertEquals("Beach, Family", state.tagsInput)
        assertEquals(trip.startDateEpochDay, state.startDateEpochDay)
    }

    @Test
    fun `loadForEdit sets error when trip missing`() = runTest(mainDispatcherRule.dispatcher) {
        every { repository.observeTrip(99L) } returns flowOf(null)
        every { repository.observeTripTagNames(99L) } returns flowOf(emptyList())

        val viewModel = TripFormViewModel(repository)
        viewModel.loadForEdit(99L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Trip not found", state.validationError)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `onLocationTextChanged clears coordinates when text differs`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TripFormViewModel(repository)
        viewModel.onPlaceSelected("Paris", 1.0, 2.0)

        viewModel.onLocationTextChanged("London")

        val state = viewModel.uiState.value
        assertNull(state.locationLat)
        assertNull(state.locationLng)
    }

    @Test
    fun `onLocationTextChanged keeps coordinates when text matches`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TripFormViewModel(repository)
        viewModel.onPlaceSelected("Rome", 1.0, 2.0)

        viewModel.onLocationTextChanged(" rome ")

        val state = viewModel.uiState.value
        assertNotNull(state.locationLat)
        assertNotNull(state.locationLng)
        assertEquals(1.0, state.locationLat ?: 0.0, 0.0)
        assertEquals(2.0, state.locationLng ?: 0.0, 0.0)
    }

    @Test
    fun `saveTrip returns validation error when title missing`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TripFormViewModel(repository)
        viewModel.onPlaceSelected("Paris", 1.0, 2.0)
        viewModel.onDateRangeChanged(10L, 12L)

        viewModel.saveTrip()

        assertEquals("Trip title is required", viewModel.uiState.value.validationError)
        coVerify(exactly = 0) { repository.upsertTripWithTags(any(), any()) }
    }

    @Test
    fun `saveTrip returns validation error for invalid location`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TripFormViewModel(repository)
        viewModel.onTitleChanged("Trip")
        viewModel.onLocationTextChanged("Paris")
        viewModel.onDateRangeChanged(10L, 12L)

        viewModel.saveTrip()

        assertEquals(
            "Please select a valid location from Google Places",
            viewModel.uiState.value.validationError
        )
        coVerify(exactly = 0) { repository.upsertTripWithTags(any(), any()) }
    }

    @Test
    fun `saveTrip returns validation error when end before start`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TripFormViewModel(repository)
        applyValidState(viewModel)
        viewModel.onDateRangeChanged(12L, 10L)

        viewModel.saveTrip()

        assertEquals(
            "End date cannot be earlier than start date",
            viewModel.uiState.value.validationError
        )
        coVerify(exactly = 0) { repository.upsertTripWithTags(any(), any()) }
    }

    @Test
    fun `saveTrip persists valid data and parses tags`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { repository.upsertTripWithTags(any(), any()) } returns 42L
        val viewModel = TripFormViewModel(repository)
        applyValidState(viewModel)
        viewModel.onTagsInputChanged("Beach, beaCh,  Summer , ")

        viewModel.saveTrip()
        advanceUntilIdle()

        val tripSlot = slot<TripEntity>()
        val tagsSlot = slot<List<String>>()
        coVerify { repository.upsertTripWithTags(capture(tripSlot), capture(tagsSlot)) }

        assertEquals(listOf("Beach", "Summer"), tagsSlot.captured)
        assertTrue(tripSlot.captured.createdAtEpochMillis > 0)
        assertTrue(tripSlot.captured.updatedAtEpochMillis >= tripSlot.captured.createdAtEpochMillis)

        val state = viewModel.uiState.value
        assertEquals(42L, state.savedTripId)
        assertTrue(state.isEditMode)
        assertTrue(!state.isSaving)
    }

    @Test
    fun `saveTrip preserves createdAt when editing`() = runTest(mainDispatcherRule.dispatcher) {
        val trip = makeTrip(id = 7L, title = "Old", createdAt = 123L)
        every { repository.observeTrip(7L) } returns flowOf(trip)
        every { repository.observeTripTagNames(7L) } returns flowOf(emptyList())
        coEvery { repository.upsertTripWithTags(any(), any()) } returns 7L

        val viewModel = TripFormViewModel(repository)
        viewModel.loadForEdit(7L)
        advanceUntilIdle()
        viewModel.onTitleChanged("Updated")

        viewModel.saveTrip()
        advanceUntilIdle()

        val tripSlot = slot<TripEntity>()
        coVerify { repository.upsertTripWithTags(capture(tripSlot), any()) }
        assertEquals(123L, tripSlot.captured.createdAtEpochMillis)
    }

    @Test
    fun `deleteTrip clears state and removes trip`() = runTest(mainDispatcherRule.dispatcher) {
        val trip = makeTrip(id = 3L, title = "Trip", createdAt = 55L)
        every { repository.observeTrip(3L) } returns flowOf(trip)
        every { repository.observeTripTagNames(3L) } returns flowOf(emptyList())

        val viewModel = TripFormViewModel(repository)
        viewModel.loadForEdit(3L)
        advanceUntilIdle()

        viewModel.deleteTrip()
        advanceUntilIdle()

        coVerify { repository.deleteTrip(trip) }
        assertEquals(TripFormUiState(), viewModel.uiState.value)
    }

    private fun applyValidState(viewModel: TripFormViewModel) {
        viewModel.onTitleChanged("Trip")
        viewModel.onPlaceSelected("Paris", 1.0, 2.0)
        viewModel.onDateRangeChanged(10L, 12L)
    }

    private fun makeTrip(
        id: Long,
        title: String,
        createdAt: Long,
        startDate: Long = 10L,
        endDate: Long = 12L
    ): TripEntity {
        return TripEntity(
            id = id,
            title = title,
            locationName = "Paris",
            locationLat = 1.0,
            locationLng = 2.0,
            startDateEpochDay = startDate,
            endDateEpochDay = endDate,
            coverImageUri = null,
            createdAtEpochMillis = createdAt,
            updatedAtEpochMillis = createdAt
        )
    }
}
