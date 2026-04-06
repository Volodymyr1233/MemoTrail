package com.example.memotrail.ui.dashboard

import com.example.memotrail.data.local.entity.TripEntity

enum class TripSortOption {
    DATE_DESC,
    LOCATION_ASC
}

data class DashboardUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val trips: List<TripEntity> = emptyList(),
    val locationFilter: String? = null,
    val sortOption: TripSortOption = TripSortOption.DATE_DESC,
    val errorMessage: String? = null
)

