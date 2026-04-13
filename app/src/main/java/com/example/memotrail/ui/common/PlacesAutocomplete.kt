package com.example.memotrail.ui.common

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

private const val TAG = "PlacesAutocomplete"

data class PlaceSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?
)

data class SelectedPlace(
    val name: String,
    val latLng: LatLng
)

suspend fun PlacesClient.fetchPredictions(query: String): List<PlaceSuggestion> = suspendCancellableCoroutine { continuation ->
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            val suggestions = response.autocompletePredictions.map { prediction ->
                PlaceSuggestion(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString()
                )
            }
            if (continuation.isActive) {
                continuation.resume(suggestions)
            }
        }
        .addOnFailureListener {
            if (continuation.isActive) {
                Log.e(TAG, "fetchPredictions failed for query '$query'", it)
                continuation.resumeWithException(it)
            }
        }
}

suspend fun PlacesClient.fetchSelectedPlace(placeId: String): SelectedPlace? = suspendCancellableCoroutine { continuation ->
    val request = FetchPlaceRequest.newInstance(
        placeId,
        listOf(Place.Field.NAME, Place.Field.LAT_LNG)
    )

    fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            val name = place.name
            val latLng = place.latLng
            if (name.isNullOrBlank() || latLng == null) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            } else {
                if (continuation.isActive) {
                    continuation.resume(SelectedPlace(name = name, latLng = latLng))
                }
            }
        }
        .addOnFailureListener {
            if (continuation.isActive) {
                Log.e(TAG, "fetchSelectedPlace failed for placeId '$placeId'", it)
                continuation.resumeWithException(it)
            }
        }
}




