package com.example.memotrail.ui.tripdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.model.MediaType
import com.example.memotrail.data.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TripDetailsViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val selectedDayId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(TripDetailsUiState())
    val uiState: StateFlow<TripDetailsUiState> = _uiState.asStateFlow()

    fun loadTrip(tripId: Long) {
        observeTripCore(tripId)
        observeSelectedDayMedia()
    }

    fun selectDay(dayId: Long?) {
        selectedDayId.value = dayId
        _uiState.update { it.copy(selectedDayId = dayId) }
    }

    fun addOrUpdateDay(day: TripDayEntity, onSaved: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val savedId = tripRepository.upsertDay(day)
            onSaved?.invoke(savedId)
            if (_uiState.value.selectedDayId == null) {
                selectDay(savedId)
            }
        }
    }

    fun deleteDay(day: TripDayEntity) {
        viewModelScope.launch {
            tripRepository.deleteDay(day)
            if (_uiState.value.selectedDayId == day.id) {
                selectDay(null)
            }
        }
    }

    fun addOrUpdateMedia(media: MediaEntryEntity) {
        viewModelScope.launch { tripRepository.upsertMedia(media) }
    }

    fun deleteMedia(media: MediaEntryEntity) {
        viewModelScope.launch { tripRepository.deleteMedia(media) }
    }

    fun observeSelectedDayMediaByType(type: MediaType): StateFlow<List<MediaEntryEntity>> {
        val mediaState = MutableStateFlow<List<MediaEntryEntity>>(emptyList())
        viewModelScope.launch {
            selectedDayId
                .flatMapLatest { dayId ->
                    if (dayId == null) flowOf(emptyList())
                    else tripRepository.observeMediaForDayByType(dayId, type)
                }
                .catch { mediaState.value = emptyList() }
                .collect { mediaState.value = it }
        }
        return mediaState.asStateFlow()
    }

    private fun observeTripCore(tripId: Long) {
        viewModelScope.launch {
            combine(
                tripRepository.observeTrip(tripId),
                tripRepository.observeTripTagNames(tripId),
                tripRepository.observeDaysForTrip(tripId)
            ) { trip: TripEntity?, tags: List<String>, days: List<TripDayEntity> ->
                Triple(trip, tags, days)
            }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Could not load trip details"
                        )
                    }
                }
                .collect { (trip, tags, days) ->
                    val preferredDay = _uiState.value.selectedDayId ?: days.firstOrNull()?.id
                    if (preferredDay != _uiState.value.selectedDayId) {
                        selectedDayId.value = preferredDay
                    }
                    _uiState.update {
                        it.copy(
                            trip = trip,
                            tagNames = tags,
                            days = days,
                            selectedDayId = preferredDay,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun observeSelectedDayMedia() {
        viewModelScope.launch {
            selectedDayId
                .flatMapLatest { dayId ->
                    if (dayId == null) flowOf(null)
                    else tripRepository.observeDayWithMedia(dayId)
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Could not load timeline media")
                    }
                }
                .collect { dayWithMedia ->
                    _uiState.update { it.copy(selectedDayWithMedia = dayWithMedia) }
                }
        }
    }

    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripDetailsViewModel::class.java)) {
                return TripDetailsViewModel(tripRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
