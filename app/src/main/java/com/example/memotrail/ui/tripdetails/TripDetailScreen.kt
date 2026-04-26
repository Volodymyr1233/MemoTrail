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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrail.R
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.model.MediaType
import com.example.memotrail.ui.common.MediaImageUseCase
import com.example.memotrail.ui.common.formatEpochDay
import com.example.memotrail.ui.common.rememberMediaImageRequest
import com.example.memotrail.ui.theme.SoftOrange

@Composable
fun TripDetailScreen(
    state: TripDetailsUiState,
    onBack: () -> Unit,
    onAddDay: () -> Unit,
    onEditDay: (Long) -> Unit,
    onDeleteDay: (TripDayEntity) -> Unit,
    onSelectDay: (Long) -> Unit,
    onOpenAudio: (Long) -> Unit,
    onOpenPhoto: (Long, Int) -> Unit,
    onOpenVideo: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMedia = state.selectedDayWithMedia?.media.orEmpty()
    val coverRequest = rememberMediaImageRequest(
        storedUri = state.trip?.coverImageUri,
        useCase = MediaImageUseCase.FEED
    )

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = coverRequest,
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
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
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
            Text(stringResource(R.string.journey_timeline_title), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onAddDay) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_day))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            itemsIndexed(state.days,  key = { _, item -> item.id }) { index, day ->
                TimelineItem(
                    index = index,
                    day = day,
                    media = if (state.selectedDayId == day.id) selectedMedia else emptyList(),
                    isSelected = state.selectedDayId == day.id,
                    onClick = { onSelectDay(day.id) },
                    onEdit = { onEditDay(day.id) },
                    onDelete = { onDeleteDay(day) },
                    onAudioClick = { onOpenAudio(day.id) },
                    onOpenPhoto = { index -> onOpenPhoto(day.id, index) },
                    onOpenVideo = { mediaId -> onOpenVideo(day.id, mediaId) },
                    position = when (index) {
                        0 -> TimelineItemPosition.FIRST
                        state.days.lastIndex -> TimelineItemPosition.LAST
                        else -> TimelineItemPosition.MIDDLE
                    }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

enum class TimelineItemPosition {
    FIRST, MIDDLE, LAST
}


@Composable
private fun TimelineItem(
    index: Int,
    position: TimelineItemPosition,
    contentStartOffset: Dp = 32.dp,
    spacerBetweenNodes: Dp = 32.dp,
    day: TripDayEntity,
    media: List<MediaEntryEntity>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAudioClick: () -> Unit,
    onOpenPhoto: (Int) -> Unit,
    onOpenVideo: (Long) -> Unit
) {

    val circleColor = MaterialTheme.colorScheme.secondary;
    val circleColorForGradient = SoftOrange;
    val paddingCircleOffset = 11.dp;
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .drawBehind {
                val circleRadiusInPx = 12.dp.toPx()
                drawCircle(
                    color = circleColor,
                    radius = circleRadiusInPx,
                    center = Offset(circleRadiusInPx, circleRadiusInPx + paddingCircleOffset.toPx())
                )

                if (position != TimelineItemPosition.LAST) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(circleColor, circleColorForGradient),
                        ),
                        start = Offset(x = circleRadiusInPx, y = circleRadiusInPx * 2 + paddingCircleOffset.toPx()),
                        end = Offset(x = circleRadiusInPx, y = this.size.height + paddingCircleOffset.toPx()),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                },
    ) {
        Card(
            modifier = Modifier.padding(
                start = contentStartOffset,
                bottom = if (position != TimelineItemPosition.LAST) {
                    spacerBetweenNodes
                } else {
                    0.dp
                }
            ),
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
                    Text(stringResource(R.string.day_label_format, index + 1), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.edit_action),
                            modifier = Modifier.clickable(onClick = onEdit),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.delete_action),
                            modifier = Modifier.clickable(onClick = onDelete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(text = formatEpochDay(day.dayDateEpochDay))
                Text(text = day.locationName)
                day.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }

                if (imageMedia.isNotEmpty()) {
                    Text(stringResource(R.string.images_label), style = MaterialTheme.typography.titleSmall)
                    MediaStrip(
                        media = imageMedia,
                        onMediaClick = { index, _ -> onOpenPhoto(index) }
                    )
                }

                if (videoMedia.isNotEmpty()) {
                    Text(stringResource(R.string.videos_label), style = MaterialTheme.typography.titleSmall)
                    MediaStrip(
                        media = videoMedia,
                        showPlayIcon = true,
                        onMediaClick = { _, item -> onOpenVideo(item.id) }
                    )
                }

                Button(onClick = onAudioClick) {
                    Icon(Icons.Outlined.KeyboardVoice, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.audio_notes))
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
            key(item.id) {
                val thumbUri = remember(item.thumbnailUri, item.uri) {
                    item.thumbnailUri ?: item.uri
                }
                val thumbnailRequest = rememberMediaImageRequest(
                    storedUri = thumbUri,
                    useCase = MediaImageUseCase.THUMBNAIL
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMediaClick(index, item) }
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (showPlayIcon && thumbnailRequest == null) {
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
                            model = thumbnailRequest,
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
}
