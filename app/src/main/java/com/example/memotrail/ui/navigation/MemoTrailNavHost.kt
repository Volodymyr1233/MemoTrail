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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.model.MediaType
import com.example.memotrail.di.AppContainer
import com.example.memotrail.ui.common.formatEpochDayIso
import com.example.memotrail.ui.common.parseIsoDateToEpochDay
import com.example.memotrail.ui.dashboard.DashboardViewModel
import com.example.memotrail.ui.dashboard.MainScreen
import com.example.memotrail.ui.dayform.DayFormContent
import com.example.memotrail.ui.map.MapScreen
import com.example.memotrail.ui.map.sampleMapPins
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
                selectedSort = uiState.sortOption,
                selectedLocation = uiState.locationFilter,
                onQueryChange = dashboardViewModel::onQueryChanged,
                onSortByDate = dashboardViewModel::onSortByDate,
                onSortByLocation = dashboardViewModel::onSortByLocation,
                onFilterByLocation = dashboardViewModel::onLocationFilterChanged,
                onOpenFilters = {},
                onTripClick = { tripId -> navController.navigate(MemoTrailDestination.TripDetail.routeFor(tripId)) },
                onEditTrip = { tripId -> navController.navigate(MemoTrailDestination.EditTrip.routeFor(tripId)) },
                onDeleteTrip = dashboardViewModel::deleteTrip
            )
        }

        composable(MemoTrailDestination.Map.route) {
            MapScreen(
                selectedPin = sampleMapPins.first(),
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
                    navController.navigate(MemoTrailDestination.TripDetail.routeFor(tripId))
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
                thumbnailUri = media?.thumbnailUri ?: media?.uri,
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDay = remember(uiState.days, dayId) { uiState.days.firstOrNull { it.id == dayId } }
    val selectedMedia = uiState.selectedDayWithMedia?.media.orEmpty()

    var dateInput by rememberSaveable(dayId, initialDate) {
        mutableStateOf(currentDay?.let { formatEpochDayIso(it.dayDateEpochDay) } ?: initialDate)
    }
    var locationInput by rememberSaveable(dayId) { mutableStateOf(currentDay?.locationName ?: "") }
    var notesInput by rememberSaveable(dayId) { mutableStateOf(currentDay?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val imageUris = remember { mutableStateListOf<String>() }
    val videoUris = remember { mutableStateListOf<String>() }

    LaunchedEffect(currentDay?.id, selectedMedia.size) {
        if (dayId != null) {
            dateInput = currentDay?.let { formatEpochDayIso(it.dayDateEpochDay) } ?: dateInput
            locationInput = currentDay?.locationName.orEmpty()
            notesInput = currentDay?.notes.orEmpty()
        }
        imageUris.clear()
        imageUris.addAll(selectedMedia.filter { it.type == MediaType.IMAGE }.map { it.uri })
        videoUris.clear()
        videoUris.addAll(selectedMedia.filter { it.type == MediaType.VIDEO }.map { it.uri })
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val text = uri.toString()
            if (text.endsWith(".mp4", ignoreCase = true) || text.contains("video", ignoreCase = true)) {
                if (!videoUris.contains(text)) videoUris.add(text)
            } else {
                if (!imageUris.contains(text)) imageUris.add(text)
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
        notes = notesInput,
        imageUris = imageUris,
        videoUris = videoUris,
        onBack = onBack,
        onSave = {
            val dayEpoch = parseIsoDateToEpochDay(dateInput) ?: currentDay?.dayDateEpochDay ?: return@DayFormContent
            val now = System.currentTimeMillis()
            viewModel.addOrUpdateDay(
                TripDayEntity(
                    id = dayId ?: 0L,
                    tripId = tripId,
                    dayDateEpochDay = dayEpoch,
                    locationName = locationInput,
                    locationLat = currentDay?.locationLat,
                    locationLng = currentDay?.locationLng,
                    notes = notesInput,
                    createdAtEpochMillis = currentDay?.createdAtEpochMillis ?: now,
                    updatedAtEpochMillis = now
                )
            ) { savedDayId ->
                imageUris.forEach { uri ->
                    viewModel.addOrUpdateMedia(
                        MediaEntryEntity(
                            tripDayId = savedDayId,
                            type = MediaType.IMAGE,
                            uri = uri,
                            thumbnailUri = uri,
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
                videoUris.forEach { uri ->
                    viewModel.addOrUpdateMedia(
                        MediaEntryEntity(
                            tripDayId = savedDayId,
                            type = MediaType.VIDEO,
                            uri = uri,
                            thumbnailUri = uri,
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            onBack()
        },
        onOpenDatePicker = { showDatePicker = true },
        onLocationChanged = { locationInput = it },
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
            val targetDayId = dayId ?: uiState.selectedDayId
            targetDayId?.let(onOpenAudioManage)
        },
        onRemoveImage = { index ->
            if (index in imageUris.indices) {
                imageUris.removeAt(index)
            }
        },
        onRemoveVideo = { index ->
            if (index in videoUris.indices) {
                videoUris.removeAt(index)
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
