package com.example.memotrail.ui.media

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.memotrail.R
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.ui.common.formatEpochMillis
import kotlinx.coroutines.delay
import kotlin.math.log10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioNotesAddScreen(
    notes: List<MediaEntryEntity>,
    onBack: () -> Unit,
    onRecordFinished: (uri: String, durationMs: Long, caption: String) -> Unit,
    onDelete: (MediaEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorder = remember { AudioRecorderController(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var isRecording by remember { mutableStateOf(false) }
    var recordingStartMs by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var currentTempUri by remember { mutableStateOf<String?>(null) }
    var amplitudeSamples by remember { mutableStateOf(List(48) { 0f }) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            runCatching {
                currentTempUri = recorder.start()
                recordingStartMs = SystemClock.elapsedRealtime()
                amplitudeSamples = List(48) { 0f }
                isRecording = true
            }.onFailure {
                isRecording = false
            }
        }
    }

    fun startRecording() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            runCatching {
                currentTempUri = recorder.start()
                recordingStartMs = SystemClock.elapsedRealtime()
                amplitudeSamples = List(48) { 0f }
                isRecording = true
            }.onFailure {
                isRecording = false
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecording() {
        val uri = recorder.stop()
        val duration = (SystemClock.elapsedRealtime() - recordingStartMs).coerceAtLeast(0L)
        isRecording = false
        elapsedMs = 0L
        recordingStartMs = 0L
        amplitudeSamples = List(48) { 0f }
        if (uri != null) {
            val caption = context.getString(
                R.string.audio_caption_format,
                formatEpochMillis(System.currentTimeMillis())
            )
            onRecordFinished(uri, duration, caption)
        }
        currentTempUri = null
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            elapsedMs = SystemClock.elapsedRealtime() - recordingStartMs
            val amplitude = recorder.readAmplitude()
            val normalized = normalizeAmplitude(amplitude)
            amplitudeSamples = (amplitudeSamples + normalized).takeLast(48)
            delay(80)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                recorder.stop()
            }
            recorder.release()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.audio_notes_add_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (isRecording) {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            stopRecording()
                        } else {
                            startRecording()
                        }
                    }
                ) {
                    if (isRecording) {
                        Icon(Icons.Outlined.Stop, contentDescription = null)
                        Text(stringResource(R.string.stop_recording_with_seconds, elapsedMs / 1000))
                    } else {
                        Icon(Icons.Outlined.FiberManualRecord, contentDescription = null)
                        Text(stringResource(R.string.record))
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RecordingWaveform(
                        levels = amplitudeSamples,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes, key = { it.id }) { note ->
                    AudioNoteListItem(
                        title = note.caption ?: stringResource(R.string.audio_note_default_title, note.id),
                        duration = note.durationMs?.let {
                            context.getString(R.string.seconds_short_format, it / 1000)
                        } ?: stringResource(R.string.unknown_label),
                        date = formatEpochMillis(note.createdAtEpochMillis),
                        trailingContent = {
                            IconButton(onClick = { onDelete(note) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete_item),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingWaveform(
    levels: List<Float>,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val idleColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        if (levels.isEmpty()) return@Canvas
        val step = size.width / levels.size
        val centerY = size.height / 2f
        val stroke = (step * 0.6f).coerceAtLeast(3f)
        levels.forEachIndexed { index, level ->
            val clamped = level.coerceIn(0.05f, 1f)
            val halfHeight = (size.height * clamped) / 2f
            val x = step * (index + 0.5f)
            drawLine(
                color = if (clamped > 0.07f) activeColor else idleColor,
                start = androidx.compose.ui.geometry.Offset(x, centerY - halfHeight),
                end = androidx.compose.ui.geometry.Offset(x, centerY + halfHeight),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun normalizeAmplitude(amplitude: Int): Float {
    if (amplitude <= 0) return 0f
    val db = (20f * log10(amplitude.toFloat() / 32767f + 1e-4f)).coerceIn(-60f, 0f)
    return ((db + 60f) / 60f).coerceIn(0f, 1f)
}

