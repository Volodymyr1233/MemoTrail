package com.example.memotrail.ui.dashboard

import com.example.memotrail.data.local.entity.TripEntity

data class DashboardUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val trips: List<TripEntity> = emptyList(),
    val errorMessage: String? = null
)

