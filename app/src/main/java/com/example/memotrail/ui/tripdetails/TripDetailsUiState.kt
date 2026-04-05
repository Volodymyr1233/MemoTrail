package com.example.memotrail.ui.tripdetails

import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.local.relation.TripDayWithMedia

data class TripDetailsUiState(
    val trip: TripEntity? = null,
    val tagNames: List<String> = emptyList(),
    val days: List<TripDayEntity> = emptyList(),
    val selectedDayId: Long? = null,
    val selectedDayWithMedia: TripDayWithMedia? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

