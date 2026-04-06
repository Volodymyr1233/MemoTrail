package com.example.memotrail.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val locationFilter = MutableStateFlow<String?>(null)
    private val sortOption = MutableStateFlow(TripSortOption.DATE_DESC)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeTrips()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        searchQuery.value = query.trim()
    }

    fun refresh() {
        searchQuery.value = _uiState.value.query.trim()
    }

    fun onSortByDate() {
        sortOption.value = TripSortOption.DATE_DESC
        _uiState.update { it.copy(sortOption = TripSortOption.DATE_DESC) }
    }

    fun onSortByLocation() {
        sortOption.value = TripSortOption.LOCATION_ASC
        _uiState.update { it.copy(sortOption = TripSortOption.LOCATION_ASC) }
    }

    fun onLocationFilterChanged(locationName: String?) {
        val normalized = locationName?.takeIf { it.isNotBlank() }
        locationFilter.value = normalized
        _uiState.update { it.copy(locationFilter = normalized) }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
        }
    }

    private fun observeTrips() {
        viewModelScope.launch {
            searchQuery
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        tripRepository.observeTrips()
                    } else {
                        tripRepository.searchTrips(query)
                    }
                }
                .combine(locationFilter) { trips, selectedLocation ->
                    if (selectedLocation == null) {
                        trips
                    } else {
                        trips.filter {
                            it.locationName.equals(selectedLocation, ignoreCase = true)
                        }
                    }
                }
                .combine(sortOption) { trips, selectedSort ->
                    when (selectedSort) {
                        TripSortOption.DATE_DESC -> trips.sortedByDescending { it.startDateEpochDay }
                        TripSortOption.LOCATION_ASC -> trips.sortedBy { it.locationName.lowercase() }
                    }
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Could not load trips"
                        )
                    }
                }
                .collect { trips ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trips = trips,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(tripRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
