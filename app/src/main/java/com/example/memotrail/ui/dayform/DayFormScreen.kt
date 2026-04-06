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
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Save
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayFormContent(
    title: String,
    date: String,
    location: String,
    notes: String,
    imageUris: List<String>,
    videoUris: List<String>,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onLocationChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onAddPhotosAndVideos: () -> Unit,
    onAddAudioNotes: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onRemoveVideo: (Int) -> Unit,
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
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChanged,
                label = { Text(stringResource(R.string.specific_location_label)) },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
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
            MediaThumbRow(items = imageUris, onRemove = onRemoveImage)

            Text(stringResource(R.string.videos_label), style = MaterialTheme.typography.titleSmall)
            MediaThumbRow(items = videoUris, onRemove = onRemoveVideo)
        }
    }
}

@Composable
private fun MediaThumbRow(
    items: List<String>,
    onRemove: (Int) -> Unit
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
                AsyncImage(
                    model = item,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
