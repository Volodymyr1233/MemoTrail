package com.example.memotrail.ui.common

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale

enum class MediaImageUseCase {
    THUMBNAIL,
    FEED,
    FULLSCREEN,
    FULLSCREEN_ZOOM
}

@Composable
fun rememberMediaImageRequest(
    storedUri: String?,
    useCase: MediaImageUseCase
): ImageRequest? {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration.screenWidthDp, density) {
        with(density) { configuration.screenWidthDp.dp.roundToPx() }
    }
    val screenHeightPx = remember(configuration.screenHeightDp, density) {
        with(density) { configuration.screenHeightDp.dp.roundToPx() }
    }

    return remember(storedUri, useCase, screenWidthPx, screenHeightPx) {
        buildMediaImageRequest(
            context = context,
            storedUri = storedUri,
            useCase = useCase,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx
        )
    }
}

fun buildMediaImageRequest(
    context: Context,
    storedUri: String?,
    useCase: MediaImageUseCase,
    screenWidthPx: Int = 1080,
    screenHeightPx: Int = 1920
): ImageRequest? {
    val model = imageModelFromStoredUri(storedUri) ?: return null

    val (width, height) = when (useCase) {
        MediaImageUseCase.THUMBNAIL -> 320 to 320
        MediaImageUseCase.FEED -> 1080 to 1080
        MediaImageUseCase.FULLSCREEN -> {
            val longest = maxOf(screenWidthPx, screenHeightPx).coerceIn(1080, 2160)
            val shortest = minOf(screenWidthPx, screenHeightPx).coerceIn(720, 1440)
            longest to shortest
        }
        MediaImageUseCase.FULLSCREEN_ZOOM -> {
            val longest = maxOf(screenWidthPx, screenHeightPx).coerceIn(2048, 4096)
            val shortest = minOf(screenWidthPx, screenHeightPx).coerceIn(1080, 2160)
            longest to shortest
        }
    }

    val isThumbnail = useCase == MediaImageUseCase.THUMBNAIL

    return ImageRequest.Builder(context)
        .data(model)
        .size(width, height)
        .scale(Scale.FILL)
        .precision(Precision.INEXACT)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .bitmapConfig(if (isThumbnail) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
        .allowHardware(!isThumbnail)
        .build()
}



