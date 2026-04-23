package com.example.memotrail.ui.tripform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.memotrail.R
import com.example.memotrail.data.media.InternalMediaStorage
import com.example.memotrail.ui.common.imageModelFromStoredUri
import com.example.memotrail.ui.common.PlaceSuggestion
import com.example.memotrail.ui.common.fetchPredictions
import com.example.memotrail.ui.common.fetchSelectedPlace
import com.example.memotrail.ui.common.formatEpochDay
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@Composable
fun TripFormRoute(
    viewModel: TripFormViewModel,
    tripIdForEdit: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val placesClient = remember(context) {
        if (Places.isInitialized()) Places.createClient(context) else null
    }

    var predictions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var isPredictionsLoading by remember { mutableStateOf(false) }
    var placesError by remember { mutableStateOf<String?>(null) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
        if (selectedUri == null) return@rememberLauncherForActivityResult

        coroutineScope.launch {
            val internalUri = InternalMediaStorage.copyImageToInternalStorage(
                context = context,
                sourceUri = selectedUri
            )
            if (internalUri == null) {
                imageUploadError = context.getString(R.string.image_import_failed)
            } else {
                imageUploadError = null
                viewModel.onCoverImageChanged(internalUri)
            }
        }
    }

    LaunchedEffect(uiState.locationName, placesClient) {
        val client = placesClient ?: run {
            predictions = emptyList()
            isPredictionsLoading = false
            placesError = context.getString(R.string.places_unavailable)
            return@LaunchedEffect
        }

        val query = uiState.locationName.trim()
        if (query.length < 2) {
            predictions = emptyList()
            isPredictionsLoading = false
            placesError = null
            return@LaunchedEffect
        }

        delay(500)
        isPredictionsLoading = true
        placesError = null
        predictions = try {
            client.fetchPredictions(query)
        } catch (_: Exception) {
            placesError = context.getString(R.string.places_fetch_failed)
            emptyList()
        }
        isPredictionsLoading = false
    }

    LaunchedEffect(tripIdForEdit) {
        tripIdForEdit?.let(viewModel::loadForEdit)
    }

    LaunchedEffect(uiState.savedTripId) {
        uiState.savedTripId?.let { tripId ->
            onSaved(tripId)
            viewModel.onSavedNavigationConsumed()
        }
    }

    val dateRangeLabel = if (uiState.startDateEpochDay != null && uiState.endDateEpochDay != null) {
        "${formatEpochDay(uiState.startDateEpochDay)} - ${formatEpochDay(uiState.endDateEpochDay)}"
    } else {
        ""
    }

    TripFormContent(
        title = if (uiState.isEditMode) stringResource(R.string.edit_trip_title) else stringResource(R.string.add_trip_title),
        tripTitle = uiState.title,
        fromDate = formatEpochDay(uiState.startDateEpochDay),
        toDate = formatEpochDay(uiState.endDateEpochDay),
        dateRangeLabel = dateRangeLabel,
        location = uiState.locationName,
        locationSelected = uiState.locationLat != null && uiState.locationLng != null,
        locationSuggestions = predictions,
        isLocationSuggestionsLoading = isPredictionsLoading,
        placesErrorMessage = placesError,
        tags = uiState.tagsInput,
        previewImageUri = uiState.coverImageUri,
        validationError = uiState.validationError?.let { raw ->
            when (raw) {
                "Trip not found" -> context.getString(R.string.trip_not_found)
                "Trip title is required" -> context.getString(R.string.trip_title_required)
                "Please select a valid location from Google Places" -> context.getString(R.string.location_select_from_places_hint)
                "Date range is required" -> context.getString(R.string.date_range_required)
                "End date cannot be earlier than start date" -> context.getString(R.string.end_date_before_start)
                else -> raw
            }
        },
        imageUploadError = imageUploadError,
        onBack = onBack,
        onTripTitleChanged = viewModel::onTitleChanged,
        onOpenFromDatePicker = { showFromDatePicker = true },
        onOpenToDatePicker = { showToDatePicker = true },
        onLocationChanged = {
            viewModel.onLocationTextChanged(it)
            predictions = emptyList()
            placesError = null
        },
        onLocationSuggestionClick = { suggestion ->
            val client = placesClient ?: return@TripFormContent
            coroutineScope.launch {
                val selected = try {
                    client.fetchSelectedPlace(suggestion.placeId)
                } catch (_: Exception) {
                    placesError = context.getString(R.string.places_fetch_failed)
                    null
                } ?: return@launch

                predictions = emptyList()
                placesError = null
                viewModel.onPlaceSelected(
                    name = selected.name,
                    lat = selected.latLng.latitude,
                    lng = selected.latLng.longitude
                )
            }
        },
        onTagsChanged = viewModel::onTagsInputChanged,
        onPickPreviewImage = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRemovePreviewImage = {
            imageUploadError = null
            viewModel.onCoverImageChanged(null)
        },
        onSave = viewModel::saveTrip,
        modifier = modifier
    )

    if (showFromDatePicker) {
        EpochDatePickerDialog(
            onDismiss = { showFromDatePicker = false },
            onDateSelected = { epochDay ->
                viewModel.onDateRangeChanged(epochDay, uiState.endDateEpochDay)
                showFromDatePicker = false
            }
        )
    }

    if (showToDatePicker) {
        EpochDatePickerDialog(
            onDismiss = { showToDatePicker = false },
            onDateSelected = { epochDay ->
                viewModel.onDateRangeChanged(uiState.startDateEpochDay, epochDay)
                showToDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormContent(
    title: String,
    tripTitle: String,
    fromDate: String,
    toDate: String,
    dateRangeLabel: String,
    location: String,
    locationSelected: Boolean,
    locationSuggestions: List<PlaceSuggestion>,
    isLocationSuggestionsLoading: Boolean,
    placesErrorMessage: String?,
    tags: String,
    previewImageUri: String?,
    validationError: String?,
    imageUploadError: String?,
    onBack: () -> Unit,
    onTripTitleChanged: (String) -> Unit,
    onOpenFromDatePicker: () -> Unit,
    onOpenToDatePicker: () -> Unit,
    onLocationChanged: (String) -> Unit,
    onLocationSuggestionClick: (PlaceSuggestion) -> Unit,
    onTagsChanged: (String) -> Unit,
    onPickPreviewImage: () -> Unit,
    onRemovePreviewImage: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasAttemptedSave by remember { mutableStateOf(false) }
    var locationDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hasAttemptedSave = true
                        onSave()
                    }) {
                        Icon(Icons.Outlined.Save, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = tripTitle,
                onValueChange = onTripTitleChanged,
                label = { Text(stringResource(R.string.trip_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedButton(onClick = onOpenFromDatePicker, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Text(if (fromDate == "-") stringResource(R.string.from_date_label) else fromDate)
            }

            OutlinedButton(onClick = onOpenToDatePicker, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Text(if (toDate == "-") stringResource(R.string.to_date_label) else toDate)
            }

            OutlinedTextField(
                value = dateRangeLabel,
                onValueChange = {},
                enabled = false,
                label = { Text(stringResource(R.string.date_range_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        onLocationChanged(it)
                        locationDropdownExpanded = true
                    },
                    label = { Text(stringResource(R.string.location_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    trailingIcon = {
                        if (isLocationSuggestionsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    },
                    supportingText = {
                        placesErrorMessage?.let {
                            Text(text = it)
                        }
                        if (hasAttemptedSave && !locationSelected) {
                            Text(stringResource(R.string.location_select_from_places_hint))
                        }
                    },
                    isError = hasAttemptedSave && !locationSelected,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),

                )

                DropdownMenu(
                    expanded = locationDropdownExpanded && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { locationDropdownExpanded = false },
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
                                locationDropdownExpanded = false
                                onLocationSuggestionClick(suggestion)
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChanged,
                label = { Text(stringResource(R.string.tags_label)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Tag, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Text(stringResource(R.string.preview_image_label), style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onPickPreviewImage)
            ) {
                AsyncImage(
                    model = imageModelFromStoredUri(previewImageUri),
                    contentDescription = stringResource(R.string.trip_preview_content_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (previewImageUri.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.upload_image),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(
                    onClick = onPickPreviewImage,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(30.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.upload_image))
                }
                IconButton(
                    onClick = onRemovePreviewImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.remove_image))
                }
            }

            imageUploadError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            if (hasAttemptedSave) {
                validationError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpochDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val pickerState = androidx.compose.material3.rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selected = pickerState.selectedDateMillis ?: return@TextButton
                val epochDay = Instant.ofEpochMilli(selected)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay()
                onDateSelected(epochDay)
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
