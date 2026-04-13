package com.example.memotrail.ui.tripform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripFormViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private var createdAtEpochMillis: Long? = null

    private val _uiState = MutableStateFlow(TripFormUiState())
    val uiState: StateFlow<TripFormUiState> = _uiState.asStateFlow()

    fun loadForEdit(tripId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, validationError = null) }

            val trip = tripRepository.observeTrip(tripId).first()
            val tags = tripRepository.observeTripTagNames(tripId).first()

            if (trip == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        validationError = "Trip not found"
                    )
                }
                return@launch
            }

            createdAtEpochMillis = trip.createdAtEpochMillis

            _uiState.update {
                it.copy(
                    tripId = trip.id,
                    title = trip.title,
                    locationName = trip.locationName,
                    locationLat = trip.locationLat,
                    locationLng = trip.locationLng,
                    startDateEpochDay = trip.startDateEpochDay,
                    endDateEpochDay = trip.endDateEpochDay,
                    coverImageUri = trip.coverImageUri,
                    tagsInput = tags.joinToString(", "),
                    isEditMode = true,
                    isLoading = false,
                    validationError = null
                )
            }
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value, validationError = null) }
    }

    fun onPlaceSelected(name: String, lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                locationName = name,
                locationLat = lat,
                locationLng = lng,
                validationError = null
            )
        }
    }

    fun onLocationTextChanged(value: String) {
        _uiState.update { state ->
            val normalized = value.trim()
            val selectedName = state.locationName.trim()
            val keepCoordinates =
                normalized.equals(selectedName, ignoreCase = true) && state.locationLat != null && state.locationLng != null

            state.copy(
                locationName = value,
                locationLat = if (keepCoordinates) state.locationLat else null,
                locationLng = if (keepCoordinates) state.locationLng else null,
                validationError = null
            )
        }
    }

    fun onDateRangeChanged(startDateEpochDay: Long?, endDateEpochDay: Long?) {
        _uiState.update {
            it.copy(
                startDateEpochDay = startDateEpochDay,
                endDateEpochDay = endDateEpochDay,
                validationError = null
            )
        }
    }

    fun onCoverImageChanged(uri: String?) {
        _uiState.update { it.copy(coverImageUri = uri) }
    }

    fun onTagsInputChanged(value: String) {
        _uiState.update { it.copy(tagsInput = value, validationError = null) }
    }

    fun saveTrip() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationError = null) }

            val now = System.currentTimeMillis()
            val trip = TripEntity(
                id = state.tripId ?: 0,
                title = state.title.trim(),
                locationName = state.locationName.trim(),
                locationLat = state.locationLat!!,
                locationLng = state.locationLng!!,
                startDateEpochDay = state.startDateEpochDay!!,
                endDateEpochDay = state.endDateEpochDay!!,
                coverImageUri = state.coverImageUri,
                createdAtEpochMillis = createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now
            )

            val tags = parseTags(state.tagsInput)
            val savedId = tripRepository.upsertTripWithTags(trip, tags)

            _uiState.update {
                it.copy(
                    tripId = savedId,
                    isEditMode = true,
                    isSaving = false,
                    savedTripId = savedId,
                    validationError = null
                )
            }
        }
    }

    fun deleteTrip() {
        val state = _uiState.value
        val tripId = state.tripId ?: return

        viewModelScope.launch {
            val trip = tripRepository.observeTrip(tripId).first() ?: return@launch
            tripRepository.deleteTrip(trip)
            _uiState.value = TripFormUiState()
            createdAtEpochMillis = null
        }
    }

    private fun validate(state: TripFormUiState): String? {
        if (state.title.isBlank()) return "Trip title is required"
        if (state.locationName.isBlank() || state.locationLat == null || state.locationLng == null) {
            return "Please select a valid location from Google Places"
        }

        val start = state.startDateEpochDay
        val end = state.endDateEpochDay

        if (start == null || end == null) return "Date range is required"
        if (end < start) return "End date cannot be earlier than start date"

        return null
    }

    private fun parseTags(input: String): List<String> {
        return input
            .split(",")
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .toList()
    }

    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripFormViewModel::class.java)) {
                return TripFormViewModel(tripRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

