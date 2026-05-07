package com.example.memotrail.ui.dashboard

import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.repository.TripRepository
import com.example.memotrail.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TripRepository
    private lateinit var allTripsFlow: MutableStateFlow<List<TripEntity>>
    private lateinit var searchTripsFlow: MutableStateFlow<List<TripEntity>>

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        allTripsFlow = MutableStateFlow(emptyList())
        searchTripsFlow = MutableStateFlow(emptyList())
        every { repository.observeTrips() } returns allTripsFlow
        every { repository.searchTrips(any()) } returns searchTripsFlow
    }

    @Test
    fun `init loads trips sorted by date desc`() = runTest(mainDispatcherRule.dispatcher) {
        val older = makeTrip(id = 1L, location = "Rome", startDate = 1L)
        val newer = makeTrip(id = 2L, location = "Paris", startDate = 5L)
        allTripsFlow.value = listOf(older, newer)

        val viewModel = DashboardViewModel(repository)
        advanceUntilIdle()

        assertEquals(listOf(newer, older), viewModel.uiState.value.trips)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onQueryChanged trims query and uses search flow`() = runTest(mainDispatcherRule.dispatcher) {
        val querySlot = slot<String>()
        every { repository.searchTrips(capture(querySlot)) } returns searchTripsFlow
        val viewModel = DashboardViewModel(repository)

        viewModel.onQueryChanged("  beach  ")
        searchTripsFlow.value = listOf(makeTrip(id = 3L, location = "Beach", startDate = 2L))
        advanceUntilIdle()

        assertEquals("beach", querySlot.captured)
        assertEquals("  beach  ", viewModel.uiState.value.query)
        assertEquals(1, viewModel.uiState.value.trips.size)
    }

    @Test
    fun `location filter narrows results ignoring case`() = runTest(mainDispatcherRule.dispatcher) {
        val rome = makeTrip(id = 1L, location = "Rome", startDate = 1L)
        val paris = makeTrip(id = 2L, location = "Paris", startDate = 2L)
        allTripsFlow.value = listOf(rome, paris)

        val viewModel = DashboardViewModel(repository)
        advanceUntilIdle()

        viewModel.onLocationFilterChanged("rome")
        advanceUntilIdle()

        assertEquals(listOf(rome), viewModel.uiState.value.trips)
    }

    @Test
    fun `onSortByLocation orders by location name`() = runTest(mainDispatcherRule.dispatcher) {
        val zurich = makeTrip(id = 1L, location = "Zurich", startDate = 1L)
        val amsterdam = makeTrip(id = 2L, location = "amsterdam", startDate = 2L)
        allTripsFlow.value = listOf(zurich, amsterdam)

        val viewModel = DashboardViewModel(repository)
        viewModel.onSortByLocation()
        advanceUntilIdle()

        assertEquals(listOf(amsterdam, zurich), viewModel.uiState.value.trips)
        assertEquals(TripSortOption.LOCATION_ASC, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `observeTrips error is surfaced in uiState`() = runTest(mainDispatcherRule.dispatcher) {
        every { repository.observeTrips() } returns flow { throw RuntimeException("boom") }

        val viewModel = DashboardViewModel(repository)
        advanceUntilIdle()

        assertEquals("boom", viewModel.uiState.value.errorMessage)
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `deleteTrip delegates to repository`() = runTest(mainDispatcherRule.dispatcher) {
        val trip = makeTrip(id = 4L, location = "Paris", startDate = 3L)
        val viewModel = DashboardViewModel(repository)

        viewModel.deleteTrip(trip)
        advanceUntilIdle()

        coVerify { repository.deleteTrip(trip) }
    }

    private fun makeTrip(id: Long, location: String, startDate: Long): TripEntity {
        return TripEntity(
            id = id,
            title = "Trip $id",
            locationName = location,
            locationLat = 1.0,
            locationLng = 2.0,
            startDateEpochDay = startDate,
            endDateEpochDay = startDate + 1,
            coverImageUri = null,
            createdAtEpochMillis = 100L + id,
            updatedAtEpochMillis = 200L + id
        )
    }
}
