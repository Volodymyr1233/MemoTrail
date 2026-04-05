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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

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
