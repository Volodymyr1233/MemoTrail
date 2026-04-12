package com.example.memotrail.ui.media

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.ui.common.formatEpochMillis
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioNotesScreen(
    notes: List<MediaEntryEntity>,
    onBack: () -> Unit,
    onDelete: (MediaEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    var activeNoteId by remember { mutableStateOf<Long?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubMs by remember { mutableLongStateOf(0L) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = player.duration.coerceAtLeast(0L)
                }
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    isScrubbing = false
                    currentMs = durationMs
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(activeNoteId) {
        while (activeNoteId != null) {
            if (!isScrubbing) {
                currentMs = player.currentPosition.coerceAtLeast(0L)
                durationMs = player.duration.coerceAtLeast(0L)
            }
            delay(200)
        }
    }

    val activeNote = notes.firstOrNull { it.id == activeNoteId } ?: notes.firstOrNull()
    val displayedCurrentMs = if (isScrubbing) scrubMs else currentMs
    val progress = remember(displayedCurrentMs, durationMs) {
        if (durationMs <= 0L) 0f else (displayedCurrentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun togglePlay(note: MediaEntryEntity) {
        val uri = Uri.parse(note.uri)
        if (activeNoteId == note.id) {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
            return
        }

        activeNoteId = note.id
        currentMs = 0L
        scrubMs = 0L
        isScrubbing = false
        durationMs = note.durationMs ?: 0L
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = true
    }

    fun onSeekChanged(newPositionMs: Float) {
        if (durationMs <= 0L) return
        isScrubbing = true
        scrubMs = newPositionMs.toLong().coerceIn(0L, durationMs)
    }

    fun onSeekFinished() {
        if (!isScrubbing) return
        val target = scrubMs.coerceIn(0L, durationMs.coerceAtLeast(0L))
        player.seekTo(target)
        currentMs = target
        isScrubbing = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Audio Notes") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        )

        activeNote?.let { current ->
            AudioPlayerCard(
                title = current.caption ?: "Audio note",
                progress = progress,
                currentTime = formatPlaybackTime(displayedCurrentMs),
                totalTime = formatPlaybackTime(if (durationMs > 0) durationMs else (current.durationMs ?: 0L)),
                isPlaying = isPlaying,
                positionMs = displayedCurrentMs,
                durationMs = durationMs,
                onSeekChanged = ::onSeekChanged,
                onSeekFinished = ::onSeekFinished,
                onPlayPause = { togglePlay(current) },
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { item ->
                AudioNoteListItem(
                    title = item.caption ?: "Audio note #${item.id}",
                    duration = item.durationMs?.let { formatPlaybackTime(it) } ?: "Unknown",
                    date = formatEpochMillis(item.createdAtEpochMillis),
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { togglePlay(item) }) {
                                Icon(
                                    if (isPlaying && activeNoteId == item.id) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = "Play"
                                )
                            }
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AudioPlayerCard(
    title: String,
    progress: Float,
    currentTime: String,
    totalTime: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onSeekChanged: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Waveform(progress = progress, modifier = Modifier.fillMaxWidth().height(48.dp))
            Slider(
                value = positionMs.toFloat(),
                onValueChange = onSeekChanged,
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                onValueChangeFinished = onSeekFinished,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currentTime, style = MaterialTheme.typography.bodySmall)
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                Text(totalTime, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Waveform(progress: Float, modifier: Modifier = Modifier) {
    val levels = listOf(0.2f, 0.5f, 0.8f, 0.35f, 0.65f, 0.9f, 0.4f, 0.55f, 0.3f, 0.7f, 0.45f, 0.6f)
    val playedColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier) {
        val step = size.width / levels.size
        levels.forEachIndexed { index, level ->
            val x = step * (index + 0.5f)
            val halfHeight = size.height * level / 2f
            val played = (index.toFloat() / levels.size) <= progress
            drawLine(
                color = if (played) playedColor else pendingColor,
                start = androidx.compose.ui.geometry.Offset(x, size.height / 2f - halfHeight),
                end = androidx.compose.ui.geometry.Offset(x, size.height / 2f + halfHeight),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AudioNoteListItem(
    title: String,
    duration: String,
    date: String,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth().clickable { }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text("$duration • $date", style = MaterialTheme.typography.bodySmall)
            }
            trailingContent()
        }
    }
}

private fun formatPlaybackTime(valueMs: Long): String {
    val totalSeconds = (valueMs / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
