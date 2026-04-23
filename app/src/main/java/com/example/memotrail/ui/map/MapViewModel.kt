package com.example.memotrail.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memotrail.data.repository.TripRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
	val pins: List<MapPinUi> = emptyList(),
	val selectedTripId: Long? = null
)

class MapViewModel(
	private val tripRepository: TripRepository
) : ViewModel() {

	private val _uiState = MutableStateFlow(MapUiState())
	val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

	init {
		observeMapPins()
	}

	fun onPinSelected(tripId: Long) {
		_uiState.update { it.copy(selectedTripId = tripId) }
	}

	private fun observeMapPins() {
		viewModelScope.launch {
			tripRepository.observeTrips().collect { trips ->
				val pins = trips.mapNotNull { trip ->
					val lat = trip.locationLat
					val lng = trip.locationLng
					if (lat == null || lng == null) {
						null
					} else {
						MapPinUi(
							tripId = trip.id,
							location = trip.locationName,
							startDateEpochDay = trip.startDateEpochDay,
							thumbnail = trip.coverImageUri,
							latLng = LatLng(lat, lng)
						)
					}
				}

				_uiState.update { current ->
					val resolvedSelectedId = when {
						pins.isEmpty() -> null
						current.selectedTripId == null -> pins.first().tripId
						pins.any { it.tripId == current.selectedTripId } -> current.selectedTripId
						else -> pins.first().tripId
					}
					current.copy(pins = pins, selectedTripId = resolvedSelectedId)
				}
			}
		}
	}

	class Factory(
		private val tripRepository: TripRepository
	) : ViewModelProvider.Factory {
		@Suppress("UNCHECKED_CAST")
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
				return MapViewModel(tripRepository) as T
			}
			throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
		}
	}
}

