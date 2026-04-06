package com.example.memotrail.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MemoTrailDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    data object Splash : MemoTrailDestination("splash", "Splash")
    data object Home : MemoTrailDestination("home", "Home", Icons.Outlined.Home)
    data object Map : MemoTrailDestination("map", "Map", Icons.Outlined.Place)
    data object AddTrip : MemoTrailDestination("trip/new", "Add", Icons.Outlined.AddCircle)
    data object Settings : MemoTrailDestination("settings", "Settings", Icons.Outlined.Settings)

    data object EditTrip : MemoTrailDestination("trip/{tripId}/edit", "Edit trip") {
        fun routeFor(tripId: Long): String = "trip/$tripId/edit"
    }

    data object TripDetail : MemoTrailDestination("trip/{tripId}", "Trip detail") {
        fun routeFor(tripId: Long): String = "trip/$tripId"
    }

    data object DayCreate : MemoTrailDestination("trip/{tripId}/day/new", "Add day") {
        fun routeFor(tripId: Long): String = "trip/$tripId/day/new"
    }

    data object DayEdit : MemoTrailDestination("trip/{tripId}/day/{dayId}/edit", "Edit day") {
        fun routeFor(tripId: Long, dayId: Long): String = "trip/$tripId/day/$dayId/edit"
    }

    data object AudioNotes : MemoTrailDestination("trip/{tripId}/audio/{dayId}", "Audio notes") {
        fun routeFor(tripId: Long, dayId: Long): String = "trip/$tripId/audio/$dayId"
    }

    data object AudioNotesManage : MemoTrailDestination("trip/{tripId}/audio/{dayId}/manage", "Manage audio") {
        fun routeFor(tripId: Long, dayId: Long): String = "trip/$tripId/audio/$dayId/manage"
    }

    data object PhotoViewer : MemoTrailDestination("trip/{tripId}/day/{dayId}/photo/{index}", "Photo") {
        fun routeFor(tripId: Long, dayId: Long, index: Int): String =
            "trip/$tripId/day/$dayId/photo/$index"
    }

    data object VideoViewer : MemoTrailDestination("trip/{tripId}/day/{dayId}/video/{mediaId}", "Video") {
        fun routeFor(tripId: Long, dayId: Long, mediaId: Long): String =
            "trip/$tripId/day/$dayId/video/$mediaId"
    }
}

val bottomNavDestinations = listOf(
    MemoTrailDestination.Home,
    MemoTrailDestination.Map,
    MemoTrailDestination.AddTrip,
    MemoTrailDestination.Settings
)


