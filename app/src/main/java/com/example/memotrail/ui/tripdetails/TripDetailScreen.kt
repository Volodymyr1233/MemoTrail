package com.example.memotrail.ui.tripdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.model.MediaType
import com.example.memotrail.ui.common.formatEpochDay
import com.example.memotrail.ui.common.imageModelFromStoredUri

@Composable
fun TripDetailScreen(
    state: TripDetailsUiState,
    onBack: () -> Unit,
    onAddDay: () -> Unit,
    onEditDay: (Long) -> Unit,
    onSelectDay: (Long) -> Unit,
    onOpenAudio: (Long) -> Unit,
    onOpenPhoto: (Long, Int) -> Unit,
    onOpenVideo: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMedia = state.selectedDayWithMedia?.media.orEmpty()

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = imageModelFromStoredUri(state.trip?.coverImageUri),
                contentDescription = state.trip?.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = state.trip?.title.orEmpty(), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(text = state.trip?.locationName.orEmpty(), color = Color.White)
                Text(
                    text = "${formatEpochDay(state.trip?.startDateEpochDay)} - ${formatEpochDay(state.trip?.endDateEpochDay)}",
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Journey Timeline", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onAddDay) {
                Icon(Icons.Outlined.Add, contentDescription = "Add day")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            items(state.days, key = { it.id }) { day ->
                TimelineItem(
                    day = day,
                    media = if (state.selectedDayId == day.id) selectedMedia else emptyList(),
                    isSelected = state.selectedDayId == day.id,
                    onClick = { onSelectDay(day.id) },
                    onEdit = { onEditDay(day.id) },
                    onAudioClick = { onOpenAudio(day.id) },
                    onOpenPhoto = { index -> onOpenPhoto(day.id, index) },
                    onOpenVideo = { mediaId -> onOpenVideo(day.id, mediaId) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TimelineItem(
    day: TripDayEntity,
    media: List<MediaEntryEntity>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onAudioClick: () -> Unit,
    onOpenPhoto: (Int) -> Unit,
    onOpenVideo: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            val imageMedia = media.filter { it.type == MediaType.IMAGE }
            val videoMedia = media.filter { it.type == MediaType.VIDEO }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Day ${day.id}", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Edit",
                        modifier = Modifier.clickable(onClick = onEdit),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(text = formatEpochDay(day.dayDateEpochDay))
                Text(text = day.locationName)
                day.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }

                if (imageMedia.isNotEmpty()) {
                    Text("Images", style = MaterialTheme.typography.titleSmall)
                    MediaStrip(
                        media = imageMedia,
                        onMediaClick = { index, _ -> onOpenPhoto(index) }
                    )
                }

                if (videoMedia.isNotEmpty()) {
                    Text("Videos", style = MaterialTheme.typography.titleSmall)
                    MediaStrip(
                        media = videoMedia,
                        showPlayIcon = true,
                        onMediaClick = { _, item -> onOpenVideo(item.id) }
                    )
                }

                Button(onClick = onAudioClick) {
                    Icon(Icons.Outlined.KeyboardVoice, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Audio Notes")
                }
            }
        }
    }
}

@Composable
private fun MediaStrip(
    media: List<MediaEntryEntity>,
    showPlayIcon: Boolean = false,
    onMediaClick: (Int, MediaEntryEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        media.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMediaClick(index, item) }
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val imageModel = imageModelFromStoredUri(item.thumbnailUri)
                if (showPlayIcon && imageModel == null) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp)
                    )
                } else {
                    AsyncImage(
                        model = imageModel ?: imageModelFromStoredUri(item.uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                if (showPlayIcon) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp)
                    )
                }
            }
        }
    }
}


