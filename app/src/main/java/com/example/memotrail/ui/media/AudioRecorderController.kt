package com.example.memotrail.ui.media

import android.content.Context
import android.os.Build
import android.media.MediaRecorder
import java.io.File
import java.io.IOException

class AudioRecorderController(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): String {
        val audioDir = File(context.filesDir, "audio_notes").apply { mkdirs() }
        val file = File(audioDir, "note_${System.currentTimeMillis()}.m4a")

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.setAudioSamplingRate(44_100)
        recorder.setAudioEncodingBitRate(128_000)
        recorder.setOutputFile(file.absolutePath)

        try {
            recorder.prepare()
            recorder.start()
        } catch (e: IOException) {
            recorder.release()
            throw IllegalStateException("Could not start recording", e)
        }

        mediaRecorder = recorder
        outputFile = file
        return file.toURI().toString()
    }

    fun stop(): String? {
        val recorder = mediaRecorder ?: return null
        return try {
            recorder.stop()
            outputFile?.toURI().toString()
        } catch (_: RuntimeException) {
            outputFile?.delete()
            null
        } finally {
            recorder.release()
            mediaRecorder = null
            outputFile = null
        }
    }

    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
        outputFile = null
    }
}


