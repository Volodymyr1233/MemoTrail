package com.example.memotrail.ui.tripform

data class TripFormUiState(
    val tripId: Long? = null,
    val title: String = "",
    val locationName: String = "",
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val startDateEpochDay: Long? = null,
    val endDateEpochDay: Long? = null,
    val coverImageUri: String? = null,
    val tagsInput: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedTripId: Long? = null,
    val validationError: String? = null
)

