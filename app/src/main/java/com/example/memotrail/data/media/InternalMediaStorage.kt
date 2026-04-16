package com.example.memotrail.data.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.memotrail.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object InternalMediaStorage {

    data class StoredMedia(
        val mediaType: MediaType,
        val storedUri: String,
        val thumbnailUri: String? = null
    )

    suspend fun copyImageToInternalStorage(
        context: Context,
        sourceUri: Uri,
        subDirectory: String = "trip_covers"
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@runCatching null
            inputStream.use { input ->
                val targetDir = File(context.filesDir, subDirectory).apply { mkdirs() }
                val extension = resolveExtension(context, sourceUri)
                val targetFile = File(targetDir, "cover_${System.currentTimeMillis()}.$extension")
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                Uri.fromFile(targetFile).toString()
            }
        }.getOrNull()
    }

    suspend fun copyMediaToInternalStorage(
        context: Context,
        sourceUri: Uri
    ): StoredMedia? = withContext(Dispatchers.IO) {
        runCatching {
            val mediaType = resolveMediaType(context, sourceUri) ?: return@runCatching null
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@runCatching null
            inputStream.use { input ->
                val targetDirName = if (mediaType == MediaType.VIDEO) "day_videos" else "day_images"
                val targetDir = File(context.filesDir, targetDirName).apply { mkdirs() }
                val extension = resolveExtension(context, sourceUri, mediaType)
                val filePrefix = if (mediaType == MediaType.VIDEO) "video" else "image"
                val targetFile = File(targetDir, "${filePrefix}_${System.currentTimeMillis()}.$extension")
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                val storedUri = Uri.fromFile(targetFile).toString()
                val thumbnailUri = if (mediaType == MediaType.VIDEO) {
                    createVideoThumbnail(context, Uri.parse(storedUri))
                } else {
                    storedUri
                }
                StoredMedia(mediaType = mediaType, storedUri = storedUri, thumbnailUri = thumbnailUri)
            }
        }.getOrNull()
    }

    private fun resolveMediaType(context: Context, sourceUri: Uri): MediaType? {
        val mimeType = context.contentResolver.getType(sourceUri).orEmpty()
        return when {
            mimeType.startsWith("image/") -> MediaType.IMAGE
            mimeType.startsWith("video/") -> MediaType.VIDEO
            else -> null
        }
    }

    private fun resolveExtension(context: Context, sourceUri: Uri, mediaType: MediaType = MediaType.IMAGE): String {
        val mimeType = context.contentResolver.getType(sourceUri).orEmpty()
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: if (mediaType == MediaType.VIDEO) "mp4" else "jpg"
    }

    private fun createVideoThumbnail(context: Context, videoUri: Uri): String? {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, videoUri)
                val frame: Bitmap = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: return@runCatching null

                val targetDir = File(context.filesDir, "day_video_thumbs").apply { mkdirs() }
                val targetFile = File(targetDir, "thumb_${System.currentTimeMillis()}.jpg")
                FileOutputStream(targetFile).use { out ->
                    frame.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }
                Uri.fromFile(targetFile).toString()
            } finally {
                retriever.release()
            }
        }.getOrNull()
    }
}

