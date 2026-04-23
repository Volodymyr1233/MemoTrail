package com.example.memotrail.ui.navigation

import com.example.memotrail.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MemoTrailDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector? = null
) {
    data object Splash : MemoTrailDestination("splash", R.string.app_name)
    data object Home : MemoTrailDestination("home", R.string.home_label, Icons.Outlined.Home)
    data object Map : MemoTrailDestination("map", R.string.map_label, Icons.Outlined.Place)
    data object AddTrip : MemoTrailDestination("trip/new", R.string.add_label, Icons.Outlined.AddCircle)
    data object Settings : MemoTrailDestination("settings", R.string.settings_nav_label, Icons.Outlined.Settings)

    data object EditTrip : MemoTrailDestination("trip/{tripId}/edit", R.string.edit_trip_title) {
        fun routeFor(tripId: Long): String = "trip/$tripId/edit"
    }

    data object TripDetail : MemoTrailDestination("trip/{tripId}", R.string.trip_title_label) {
        fun routeFor(tripId: Long): String = "trip/$tripId"
    }

    data object DayCreate : MemoTrailDestination("trip/{tripId}/day/new", R.string.day_date_label) {
        fun routeFor(tripId: Long): String = "trip/$tripId/day/new"
    }

    data object DayEdit : MemoTrailDestination("trip/{tripId}/day/{dayId}/edit", R.string.day_date_label) {
        fun routeFor(tripId: Long, dayId: Long): String = "trip/$tripId/day/$dayId/edit"
    }

    data object AudioNotes : MemoTrailDestination("trip/{tripId}/audio/{dayId}", R.string.audio_notes) {
        fun routeFor(tripId: Long, dayId: Long): String = "trip/$tripId/audio/$dayId"
    }

    data object AudioNotesManage : MemoTrailDestination("trip/{tripId}/audio/{dayId}/manage", R.string.audio_notes_add_title) {
        fun routeFor(tripId: Long, dayId: Long): String = "trip/$tripId/audio/$dayId/manage"
    }

    data object PhotoViewer : MemoTrailDestination("trip/{tripId}/day/{dayId}/photo/{index}", R.string.trip_preview_content_description) {
        fun routeFor(tripId: Long, dayId: Long, index: Int): String =
            "trip/$tripId/day/$dayId/photo/$index"
    }

    data object VideoViewer : MemoTrailDestination("trip/{tripId}/day/{dayId}/video/{mediaId}", R.string.videos_label) {
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


