package com.example.memotrail.ui.navigation

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.media.InternalMediaStorage
import com.example.memotrail.data.model.MediaType
import com.example.memotrail.R
import com.example.memotrail.di.AppContainer
import com.example.memotrail.ui.common.PlaceSuggestion
import com.example.memotrail.ui.common.fetchPredictions
import com.example.memotrail.ui.common.fetchSelectedPlace
import com.example.memotrail.ui.common.formatEpochDayIso
import com.example.memotrail.ui.common.parseIsoDateToEpochDay
import com.example.memotrail.ui.dashboard.DashboardViewModel
import com.example.memotrail.ui.dashboard.MainScreen
import com.example.memotrail.ui.dayform.DayFormContent
import com.example.memotrail.ui.map.MapScreen
import com.example.memotrail.ui.map.MapViewModel
import com.example.memotrail.ui.media.AudioNotesAddScreen
import com.example.memotrail.ui.media.AudioNotesScreen
import com.example.memotrail.ui.media.PhotoViewerScreen
import com.example.memotrail.ui.media.VideoPlayScreen
import com.example.memotrail.ui.settings.SettingsScreen
import com.example.memotrail.ui.settings.SettingsViewModel
import com.example.memotrail.ui.splash.SplashScreen
import com.example.memotrail.ui.tripdetails.TripDetailScreen
import com.example.memotrail.ui.tripdetails.TripDetailsViewModel
import com.example.memotrail.ui.tripform.TripFormRoute
import com.example.memotrail.ui.tripform.TripFormViewModel
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@Composable
fun MemoTrailNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MemoTrailDestination.Splash.route,
        modifier = modifier
    ) {
        composable(MemoTrailDestination.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(MemoTrailDestination.Home.route) {
                        popUpTo(MemoTrailDestination.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MemoTrailDestination.Home.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
            MainScreen(
                query = uiState.query,
                trips = uiState.trips,
                onQueryChange = dashboardViewModel::onQueryChanged,
                onTripClick = { tripId -> navController.navigate(MemoTrailDestination.TripDetail.routeFor(tripId)) },
                onEditTrip = { tripId -> navController.navigate(MemoTrailDestination.EditTrip.routeFor(tripId)) },
                onDeleteTrip = dashboardViewModel::deleteTrip
            )
        }

        composable(MemoTrailDestination.Map.route) {
            val mapViewModel: MapViewModel = viewModel(
                factory = MapViewModel.Factory(appContainer.tripRepository)
            )
            val mapState by mapViewModel.uiState.collectAsStateWithLifecycle()

            MapScreen(
                pins = mapState.pins,
                selectedTripId = mapState.selectedTripId,
                onPinSelected = mapViewModel::onPinSelected,
                onViewTripClick = { tripId ->
                    navController.navigate(MemoTrailDestination.TripDetail.routeFor(tripId))
                }
            )
        }

        composable(MemoTrailDestination.AddTrip.route) {
            val viewModel: TripFormViewModel = viewModel(
                factory = TripFormViewModel.Factory(appContainer.tripRepository)
            )
            TripFormRoute(
                viewModel = viewModel,
                tripIdForEdit = null,
                onBack = { navController.popBackStack() },
                onSaved = { tripId ->
                    navController.navigate(MemoTrailDestination.TripDetail.routeFor(tripId)) {
                        popUpTo(MemoTrailDestination.AddTrip.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MemoTrailDestination.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(appContainer.userPreferencesRepository)
            )
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                darkModeEnabled = settingsState.darkModeEnabled,
                selectedLanguage = settingsState.languageTag,
                onDarkModeToggle = settingsViewModel::onDarkModeChanged,
                onLanguageSelected = settingsViewModel::onLanguageChanged,
                onAboutClick = {}
            )
        }

        composable(
            route = MemoTrailDestination.EditTrip.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val viewModel: TripFormViewModel = viewModel(
                factory = TripFormViewModel.Factory(appContainer.tripRepository)
            )
            TripFormRoute(
                viewModel = viewModel,
                tripIdForEdit = tripId,
                onBack = { navController.popBackStack() },
                onSaved = { savedId ->
                    navController.navigate(MemoTrailDestination.TripDetail.routeFor(savedId)) {
                        popUpTo(MemoTrailDestination.Home.route)
                    }
                }
            )
        }

        composable(
            route = MemoTrailDestination.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

            TripDetailScreen(
                state = uiState,
                onBack = { navController.popBackStack() },
                onAddDay = { navController.navigate(MemoTrailDestination.DayCreate.routeFor(tripId)) },
                onEditDay = { dayId -> navController.navigate(MemoTrailDestination.DayEdit.routeFor(tripId, dayId)) },
                onSelectDay = viewModel::selectDay,
                onOpenAudio = { dayId -> navController.navigate(MemoTrailDestination.AudioNotes.routeFor(tripId, dayId)) },
                onOpenPhoto = { dayId, index -> navController.navigate(MemoTrailDestination.PhotoViewer.routeFor(tripId, dayId, index)) },
                onOpenVideo = { dayId, mediaId -> navController.navigate(MemoTrailDestination.VideoViewer.routeFor(tripId, dayId, mediaId)) }
            )
        }

        composable(
            route = MemoTrailDestination.DayCreate.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

            DayFormRoute(
                tripId = tripId,
                dayId = null,
                viewModel = viewModel,
                initialDate = formatEpochDayIso(uiState.trip?.startDateEpochDay),
                onBack = { navController.popBackStack() },
                onOpenAudioManage = { dayIdForAudio ->
                    navController.navigate(MemoTrailDestination.AudioNotesManage.routeFor(tripId, dayIdForAudio))
                }
            )
        }

        composable(
            route = MemoTrailDestination.DayEdit.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("dayId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val dayId = backStackEntry.arguments?.getLong("dayId") ?: return@composable
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId, dayId) {
                viewModel.loadTrip(tripId)
                viewModel.selectDay(dayId)
            }

            DayFormRoute(
                tripId = tripId,
                dayId = dayId,
                viewModel = viewModel,
                initialDate = uiState.days.firstOrNull { it.id == dayId }?.let { formatEpochDayIso(it.dayDateEpochDay) } ?: "",
                onBack = { navController.popBackStack() },
                onOpenAudioManage = { dayIdForAudio ->
                    navController.navigate(MemoTrailDestination.AudioNotesManage.routeFor(tripId, dayIdForAudio))
                }
            )
        }

        composable(
            route = MemoTrailDestination.AudioNotes.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("dayId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val dayId = backStackEntry.arguments?.getLong("dayId") ?: return@composable
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId, dayId) {
                viewModel.loadTrip(tripId)
                viewModel.selectDay(dayId)
            }

            val audioNotes = uiState.selectedDayWithMedia?.media?.filter { it.type == MediaType.AUDIO }.orEmpty()
            AudioNotesScreen(
                notes = audioNotes,
                onBack = { navController.popBackStack() },
                onDelete = viewModel::deleteMedia
            )
        }

        composable(
            route = MemoTrailDestination.AudioNotesManage.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("dayId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val dayId = backStackEntry.arguments?.getLong("dayId") ?: return@composable
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId, dayId) {
                viewModel.loadTrip(tripId)
                viewModel.selectDay(dayId)
            }

            val audioNotes = uiState.selectedDayWithMedia?.media?.filter { it.type == MediaType.AUDIO }.orEmpty()
            AudioNotesAddScreen(
                notes = audioNotes,
                onBack = { navController.popBackStack() },
                onRecordFinished = { uri, durationMs, caption ->
                    viewModel.addOrUpdateMedia(
                        MediaEntryEntity(
                            tripDayId = dayId,
                            type = MediaType.AUDIO,
                            uri = uri,
                            durationMs = durationMs,
                            caption = caption,
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                },
                onDelete = viewModel::deleteMedia
            )
        }

        composable(
            route = MemoTrailDestination.PhotoViewer.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("dayId") { type = NavType.LongType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val dayId = backStackEntry.arguments?.getLong("dayId") ?: return@composable
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId, dayId) {
                viewModel.loadTrip(tripId)
                viewModel.selectDay(dayId)
            }

            val images = uiState.selectedDayWithMedia?.media
                ?.filter { it.type == MediaType.IMAGE }
                ?.map { it.uri }
                .orEmpty()
            PhotoViewerScreen(
                imageUris = images,
                selectedIndex = index,
                onClose = { navController.popBackStack() },
                onSelect = { selectedIndex ->
                    navController.navigate(MemoTrailDestination.PhotoViewer.routeFor(tripId, dayId, selectedIndex)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = MemoTrailDestination.VideoViewer.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("dayId") { type = NavType.LongType },
                navArgument("mediaId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val dayId = backStackEntry.arguments?.getLong("dayId") ?: return@composable
            val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: return@composable
            val viewModel: TripDetailsViewModel = viewModel(
                factory = TripDetailsViewModel.Factory(appContainer.tripRepository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(tripId, dayId) {
                viewModel.loadTrip(tripId)
                viewModel.selectDay(dayId)
            }

            val media = uiState.selectedDayWithMedia?.media?.firstOrNull { it.id == mediaId }
            VideoPlayScreen(
                tripTitle = uiState.trip?.title ?: "Video",
                videoUri = media?.uri,
                onClose = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayFormRoute(
    tripId: Long,
    dayId: Long?,
    viewModel: TripDetailsViewModel,
    initialDate: String,
    onBack: () -> Unit,
    onOpenAudioManage: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val placesClient = remember(context) {
        if (Places.isInitialized()) Places.createClient(context) else null
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDay = remember(uiState.days, dayId) { uiState.days.firstOrNull { it.id == dayId } }
    val currentDayMedia = remember(dayId, uiState.selectedDayWithMedia?.day?.id, uiState.selectedDayWithMedia?.media) {
        if (dayId != null && uiState.selectedDayWithMedia?.day?.id == dayId) {
            uiState.selectedDayWithMedia?.media.orEmpty()
        } else {
            emptyList()
        }
    }

    var dateInput by rememberSaveable(dayId, initialDate) {
        mutableStateOf(currentDay?.let { formatEpochDayIso(it.dayDateEpochDay) } ?: initialDate)
    }
    var locationInput by rememberSaveable(dayId) { mutableStateOf(currentDay?.locationName ?: "") }
    var locationLat by rememberSaveable(dayId) { mutableStateOf(currentDay?.locationLat) }
    var locationLng by rememberSaveable(dayId) { mutableStateOf(currentDay?.locationLng) }
    var locationSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var isLocationSuggestionsLoading by remember { mutableStateOf(false) }
    var placesErrorMessage by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSave by rememberSaveable(dayId) { mutableStateOf(false) }
    var notesInput by rememberSaveable(dayId) { mutableStateOf(currentDay?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val imageUris = remember(dayId) { mutableStateListOf<String>() }
    val videoUris = remember(dayId) { mutableStateListOf<String>() }
    val videoThumbnailUris = remember(dayId) { mutableStateMapOf<String, String?>() }

    LaunchedEffect(dayId, currentDay?.id, currentDayMedia.size) {
        if (dayId != null) {
            dateInput = currentDay?.let { formatEpochDayIso(it.dayDateEpochDay) } ?: dateInput
            locationInput = currentDay?.locationName.orEmpty()
            locationLat = currentDay?.locationLat
            locationLng = currentDay?.locationLng
            notesInput = currentDay?.notes.orEmpty()
        }
        hasAttemptedSave = false
        imageUris.clear()
        imageUris.addAll(currentDayMedia.filter { it.type == MediaType.IMAGE }.map { it.uri })
        videoUris.clear()
        videoUris.addAll(currentDayMedia.filter { it.type == MediaType.VIDEO }.map { it.uri })
        videoThumbnailUris.clear()
        currentDayMedia.filter { it.type == MediaType.VIDEO }.forEach { media ->
            videoThumbnailUris[media.uri] = media.thumbnailUri
        }
    }

    LaunchedEffect(locationInput, placesClient) {
        val client = placesClient ?: run {
            locationSuggestions = emptyList()
            isLocationSuggestionsLoading = false
            placesErrorMessage = context.getString(R.string.places_unavailable)
            return@LaunchedEffect
        }

        val query = locationInput.trim()
        if (query.length < 2) {
            locationSuggestions = emptyList()
            isLocationSuggestionsLoading = false
            placesErrorMessage = null
            return@LaunchedEffect
        }

        delay(500)
        isLocationSuggestionsLoading = true
        placesErrorMessage = null
        locationSuggestions = try {
            client.fetchPredictions(query)
        } catch (_: Exception) {
            placesErrorMessage = context.getString(R.string.places_fetch_failed)
            emptyList()
        }
        isLocationSuggestionsLoading = false
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        coroutineScope.launch {
            uris.forEach { uri ->
                val storedMedia = InternalMediaStorage.copyMediaToInternalStorage(
                    context = context,
                    sourceUri = uri
                ) ?: return@forEach

                when (storedMedia.mediaType) {
                    MediaType.IMAGE -> if (!imageUris.contains(storedMedia.storedUri)) imageUris.add(storedMedia.storedUri)
                    MediaType.VIDEO -> {
                        if (!videoUris.contains(storedMedia.storedUri)) videoUris.add(storedMedia.storedUri)
                        videoThumbnailUris[storedMedia.storedUri] = storedMedia.thumbnailUri
                    }
                    MediaType.AUDIO -> Unit
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val canOpenPicker = granted.values.all { it }
        if (canOpenPicker) {
            mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
    }

    DayFormContent(
        title = if (dayId == null) "Add Day" else "Edit Day",
        date = dateInput,
        location = locationInput,
        isLocationSelected = locationLat != null && locationLng != null,
        locationSuggestions = locationSuggestions,
        isLocationSuggestionsLoading = isLocationSuggestionsLoading,
        placesErrorMessage = placesErrorMessage,
        showLocationValidation = hasAttemptedSave,
        notes = notesInput,
        imageUris = imageUris,
        videoUris = videoUris,
        videoThumbnailUris = videoThumbnailUris,
        onBack = onBack,
        onSave = {
            hasAttemptedSave = true
            if (locationInput.isBlank() || locationLat == null || locationLng == null) {
                return@DayFormContent
            }
            val dayEpoch = parseIsoDateToEpochDay(dateInput) ?: currentDay?.dayDateEpochDay ?: return@DayFormContent
            val now = System.currentTimeMillis()
            viewModel.addOrUpdateDay(
                TripDayEntity(
                    id = dayId ?: 0L,
                    tripId = tripId,
                    dayDateEpochDay = dayEpoch,
                    locationName = locationInput,
                    locationLat = locationLat,
                    locationLng = locationLng,
                    notes = notesInput,
                    createdAtEpochMillis = currentDay?.createdAtEpochMillis ?: now,
                    updatedAtEpochMillis = now
                )
            ) { savedDayId ->
                val effectiveDayId = if (dayId != null && dayId > 0L) dayId else savedDayId
                if (effectiveDayId <= 0L) {
                    return@addOrUpdateDay
                }

                val existingImages = currentDayMedia.filter { it.type == MediaType.IMAGE }
                val existingVideos = currentDayMedia.filter { it.type == MediaType.VIDEO }
                val targetImageUris = imageUris.distinct()
                val targetVideoUris = videoUris.distinct()

                existingImages
                    .filter { it.uri !in targetImageUris }
                    .forEach(viewModel::deleteMedia)
                existingVideos
                    .filter { it.uri !in targetVideoUris }
                    .forEach(viewModel::deleteMedia)

                val existingImageByUri = existingImages.associateBy { it.uri }
                val existingVideoByUri = existingVideos.associateBy { it.uri }

                targetImageUris.forEach { uri ->
                    val existing = existingImageByUri[uri]
                    viewModel.addOrUpdateMedia(
                        MediaEntryEntity(
                            id = existing?.id ?: 0L,
                            tripDayId = effectiveDayId,
                            type = MediaType.IMAGE,
                            uri = uri,
                            thumbnailUri = uri,
                            durationMs = existing?.durationMs,
                            caption = existing?.caption,
                            pinLat = existing?.pinLat,
                            pinLng = existing?.pinLng,
                            createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis()
                        )
                    )
                }
                targetVideoUris.forEach { uri ->
                    val existing = existingVideoByUri[uri]
                    viewModel.addOrUpdateMedia(
                        MediaEntryEntity(
                            id = existing?.id ?: 0L,
                            tripDayId = effectiveDayId,
                            type = MediaType.VIDEO,
                            uri = uri,
                            thumbnailUri = videoThumbnailUris[uri] ?: existing?.thumbnailUri,
                            durationMs = existing?.durationMs,
                            caption = existing?.caption,
                            pinLat = existing?.pinLat,
                            pinLng = existing?.pinLng,
                            createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            }
            onBack()
        },
        onOpenDatePicker = { showDatePicker = true },
        onLocationChanged = {
            locationInput = it
            locationLat = null
            locationLng = null
            locationSuggestions = emptyList()
            placesErrorMessage = null
        },
        onLocationSuggestionClick = { suggestion ->
            val client = placesClient ?: return@DayFormContent
            coroutineScope.launch {
                val selected = try {
                    client.fetchSelectedPlace(suggestion.placeId)
                } catch (_: Exception) {
                    placesErrorMessage = context.getString(R.string.places_fetch_failed)
                    null
                } ?: return@launch

                locationInput = selected.name
                locationLat = selected.latLng.latitude
                locationLng = selected.latLng.longitude
                locationSuggestions = emptyList()
                placesErrorMessage = null
            }
        },
        onNotesChanged = { notesInput = it },
        onAddPhotosAndVideos = {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            permissionLauncher.launch(permissions)
        },
        onAddAudioNotes = {
            dayId?.let(onOpenAudioManage)
        },
        onRemoveImage = { index ->
            if (index in imageUris.indices) {
                imageUris.removeAt(index)
            }
        },
        onRemoveVideo = { index ->
            if (index in videoUris.indices) {
                val removedUri = videoUris.removeAt(index)
                videoThumbnailUris.remove(removedUri)
            }
        }
    )

    if (showDatePicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = pickerState.selectedDateMillis ?: return@TextButton
                    dateInput = Instant.ofEpochMilli(selected)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
