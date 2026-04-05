package com.example.memotrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memotrail.ui.dashboard.DashboardViewModel
import com.example.memotrail.ui.theme.MemoTrailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as MemoTrailApplication).container

        setContent {
            MemoTrailTheme {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(appContainer.tripRepository)
                )
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardStatePreview(
                        modifier = Modifier.padding(innerPadding),
                        tripCount = uiState.trips.size,
                        query = uiState.query,
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStatePreview(
    modifier: Modifier = Modifier,
    tripCount: Int,
    query: String,
    isLoading: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "MemoTrail",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(text = "State Layer wired")
        Text(text = "Trips in database: $tripCount")
        Text(text = "Search query: $query")
        Text(text = "Loading: $isLoading")
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardStatePreviewPreview() {
    MemoTrailTheme {
        DashboardStatePreview(
            tripCount = 3,
            query = "alps",
            isLoading = false
        )
    }
}