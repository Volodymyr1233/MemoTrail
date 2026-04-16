package com.example.memotrail.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.ui.common.formatEpochDay
import com.example.memotrail.ui.common.imageModelFromStoredUri
import com.example.memotrail.ui.dashboard.TripSortOption

@Composable
fun MainScreen(
    query: String,
    trips: List<TripEntity>,
    selectedSort: TripSortOption,
    selectedLocation: String?,
    onQueryChange: (String) -> Unit,
    onSortByDate: () -> Unit,
    onSortByLocation: () -> Unit,
    onFilterByLocation: (String?) -> Unit,
    onOpenFilters: () -> Unit,
    onTripClick: (Long) -> Unit,
    onEditTrip: (Long) -> Unit,
    onDeleteTrip: (TripEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "MemoTrail", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search trips") },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            singleLine = true
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = onSortByDate,
                label = { Text("Sort: Date") },
                leadingIcon = if (selectedSort == TripSortOption.DATE_DESC) {
                    { Icon(Icons.Outlined.Search, contentDescription = null) }
                } else null
            )
            AssistChip(
                onClick = onSortByLocation,
                label = { Text("Sort: Location") },
                leadingIcon = if (selectedSort == TripSortOption.LOCATION_ASC) {
                    { Icon(Icons.Outlined.Search, contentDescription = null) }
                } else null
            )
            AssistChip(
                onClick = { onFilterByLocation(null) },
                label = { Text("All locations") }
            )
            trips.map { it.locationName }.distinct().take(4).forEach { location ->
                AssistChip(
                    onClick = { onFilterByLocation(location) },
                    label = { Text(location) },
                    leadingIcon = if (selectedLocation == location) {
                        { Icon(Icons.Outlined.Place, contentDescription = null) }
                    } else null
                )
            }
            AssistChip(onClick = onOpenFilters, label = { Text("Filters") })
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trips, key = { it.id }) { trip ->
                TripCard(
                    trip = trip,
                    photoCount = 0,
                    onClick = { onTripClick(trip.id) },
                    onEditClick = { onEditTrip(trip.id) },
                    onDeleteClick = { onDeleteTrip(trip) }
                )
            }
        }
    }
}

@Composable
fun TripCard(
    trip: TripEntity,
    photoCount: Int,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(250.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageModelFromStoredUri(trip.coverImageUri),
                contentDescription = trip.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )

            if (trip.coverImageUri.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Outlined.Photo,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trip.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = trip.locationName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "${formatEpochDay(trip.startDateEpochDay)} - ${formatEpochDay(trip.endDateEpochDay)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$photoCount photos",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Edit trip",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete trip",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

