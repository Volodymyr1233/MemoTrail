package com.example.memotrail.ui.dayform

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrail.R
import com.example.memotrail.ui.common.imageModelFromStoredUri
import com.example.memotrail.ui.common.PlaceSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayFormContent(
    title: String,
    date: String,
    location: String,
    isLocationSelected: Boolean,
    locationSuggestions: List<PlaceSuggestion>,
    isLocationSuggestionsLoading: Boolean,
    placesErrorMessage: String?,
    showLocationValidation: Boolean,
    notes: String,
    imageUris: List<String>,
    videoUris: List<String>,
    videoThumbnailUris: Map<String, String?>,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onLocationChanged: (String) -> Unit,
    onLocationSuggestionClick: (PlaceSuggestion) -> Unit,
    onNotesChanged: (String) -> Unit,
    onAddPhotosAndVideos: () -> Unit,
    onAddAudioNotes: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onRemoveVideo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationDropdownExpanded = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onOpenDatePicker, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.EditCalendar, contentDescription = null)
                Text(if (date.isBlank()) stringResource(R.string.day_date_label) else date)
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        onLocationChanged(it)
                        locationDropdownExpanded.value = true
                    },
                    label = { Text(stringResource(R.string.specific_location_label)) },
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    trailingIcon = {
                        if (isLocationSuggestionsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    },
                    supportingText = {
                        placesErrorMessage?.let {
                            Text(it)
                        }
                        if (showLocationValidation && !isLocationSelected) {
                            Text(stringResource(R.string.location_select_from_places_hint))
                        }
                    },
                    isError = showLocationValidation && !isLocationSelected,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = locationDropdownExpanded.value && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { locationDropdownExpanded.value = false },
                    modifier = Modifier.fillMaxWidth(0.93f)
                ) {
                    locationSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = suggestion.primaryText)
                                    suggestion.secondaryText?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            onClick = {
                                locationDropdownExpanded.value = false
                                onLocationSuggestionClick(suggestion)
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text(stringResource(R.string.notes_label)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onAddPhotosAndVideos, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                    Text(stringResource(R.string.add_photos_videos))
                }
                OutlinedButton(onClick = onAddAudioNotes, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.KeyboardVoice, contentDescription = null)
                    Text(stringResource(R.string.audio_notes))
                }
            }

            Text(stringResource(R.string.images_label), style = MaterialTheme.typography.titleSmall)
            MediaThumbRow(
                items = imageUris,
                onRemove = onRemoveImage,
                displayAsVideo = false,
                thumbnailUriProvider = { null }
            )

            Text(stringResource(R.string.videos_label), style = MaterialTheme.typography.titleSmall)
            MediaThumbRow(
                items = videoUris,
                onRemove = onRemoveVideo,
                displayAsVideo = true,
                thumbnailUriProvider = { videoThumbnailUris[it] }
            )
        }
    }
}

@Composable
private fun MediaThumbRow(
    items: List<String>,
    onRemove: (Int) -> Unit,
    displayAsVideo: Boolean,
    thumbnailUriProvider: (String) -> String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (displayAsVideo) {
                    val thumbnailModel = imageModelFromStoredUri(thumbnailUriProvider(item))
                    if (thumbnailModel != null) {
                        AsyncImage(
                            model = thumbnailModel,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircleOutline,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(36.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = imageModelFromStoredUri(item),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                IconButton(
                    onClick = { onRemove(index) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.delete_item))
                }
            }
        }
    }
}
