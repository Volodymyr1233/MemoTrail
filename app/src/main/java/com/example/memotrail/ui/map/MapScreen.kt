package com.example.memotrail.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrail.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    selectedPin: MapPinUi,
    onViewTripClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var activePin by remember { mutableStateOf(selectedPin) }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(activePin.latLng, 4.8f)
    }

    Column(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            sampleMapPins.forEach { pin ->
                Marker(
                    state = MarkerState(position = pin.latLng),
                    title = pin.location,
                    snippet = pin.date,
                    onClick = {
                        activePin = pin
                        cameraPositionState.move(CameraUpdateFactory.newLatLng(pin.latLng))
                        true
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = activePin.thumbnail,
                    contentDescription = activePin.location,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = activePin.location, style = MaterialTheme.typography.titleMedium)
                    Text(text = activePin.date, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onViewTripClick(activePin.tripId) }) {
                        Text(stringResource(R.string.view_trip))
                    }
                }
            }
        }
    }
}

data class MapPinUi(
    val tripId: Long,
    val location: String,
    val date: String,
    val thumbnail: String?,
    val latLng: LatLng
)

val sampleMapPins = listOf(
    MapPinUi(1, "Swiss Alps", "12 Jul 2025", null, LatLng(46.8182, 8.2275)),
    MapPinUi(2, "Paris", "18 Jul 2025", null, LatLng(48.8566, 2.3522)),
    MapPinUi(3, "Lisbon", "02 Aug 2025", null, LatLng(38.7223, -9.1393))
)
