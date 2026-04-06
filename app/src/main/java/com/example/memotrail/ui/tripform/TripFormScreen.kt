package com.example.memotrail.ui.tripform

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.memotrail.ui.common.formatEpochDay
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.time.Instant
import java.time.LocalDate
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

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    val placesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data ?: Intent())
            viewModel.onLocationChanged(
                value = place.name.orEmpty(),
                lat = place.latLng?.latitude,
                lng = place.latLng?.longitude
            )
        }
    }

    LaunchedEffect(tripIdForEdit) {
        tripIdForEdit?.let(viewModel::loadForEdit)
    }

    LaunchedEffect(uiState.savedTripId) {
        uiState.savedTripId?.let(onSaved)
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
        tags = uiState.tagsInput,
        previewImageUri = uiState.coverImageUri,
        validationError = uiState.validationError,
        onBack = onBack,
        onTripTitleChanged = viewModel::onTitleChanged,
        onOpenFromDatePicker = { showFromDatePicker = true },
        onOpenToDatePicker = { showToDatePicker = true },
        onOpenLocationAutocomplete = {
            if (!Places.isInitialized()) {
                val key = context.getString(R.string.google_maps_api_key)
                if (key.isNotBlank() && !key.contains("REPLACE", ignoreCase = true)) {
                    Places.initialize(context, key)
                }
            }
            if (Places.isInitialized()) {
                val fields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(context)
                placesLauncher.launch(intent)
            }
        },
        onLocationChanged = { viewModel.onLocationChanged(it) },
        onTagsChanged = viewModel::onTagsInputChanged,
        onRemovePreviewImage = { viewModel.onCoverImageChanged(null) },
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
    tags: String,
    previewImageUri: String?,
    validationError: String?,
    onBack: () -> Unit,
    onTripTitleChanged: (String) -> Unit,
    onOpenFromDatePicker: () -> Unit,
    onOpenToDatePicker: () -> Unit,
    onOpenLocationAutocomplete: () -> Unit,
    onLocationChanged: (String) -> Unit,
    onTagsChanged: (String) -> Unit,
    onRemovePreviewImage: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = onLocationChanged,
                label = { Text(stringResource(R.string.location_label)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = onOpenLocationAutocomplete)
                    )
                },
                singleLine = true
            )
            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChanged,
                label = { Text(stringResource(R.string.tags_label)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Tag, contentDescription = null) },
                singleLine = true
            )

            Text(stringResource(R.string.preview_image_label), style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = previewImageUri,
                    contentDescription = stringResource(R.string.trip_preview_content_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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

            validationError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
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
