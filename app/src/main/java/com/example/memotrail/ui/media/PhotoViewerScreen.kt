package com.example.memotrail.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import com.example.memotrail.R
import com.example.memotrail.ui.common.MediaImageUseCase
import com.example.memotrail.ui.common.rememberMediaImageRequest

@Composable
fun PhotoViewerScreen(
    imageUris: List<String>,
    selectedIndex: Int,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeIndex = selectedIndex.coerceIn(0, (imageUris.size - 1).coerceAtLeast(0))
    val selectedUri = imageUris.getOrNull(safeIndex)
    var scale by remember { mutableFloatStateOf(1f) }
    var highResEnabled by remember(selectedUri) { mutableStateOf(false) }
    var showHighResLayer by remember(selectedUri) { mutableStateOf(false) }

    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
    }

    LaunchedEffect(scale) {
        when {
            !highResEnabled && scale > 2.4f -> highResEnabled = true
            highResEnabled && scale < 1.9f -> {
                highResEnabled = false
                showHighResLayer = false
            }
        }
    }

    val baseImageRequest = rememberMediaImageRequest(
        storedUri = selectedUri,
        useCase = MediaImageUseCase.FULLSCREEN
    )

    val zoomImageRequest = if (highResEnabled) {
        rememberMediaImageRequest(
            storedUri = selectedUri,
            useCase = MediaImageUseCase.FULLSCREEN_ZOOM
        )
    } else {
        null
    }

    val basePainter = rememberAsyncImagePainter(model = baseImageRequest)
    val zoomPainter = rememberAsyncImagePainter(model = zoomImageRequest)

    LaunchedEffect(highResEnabled, zoomPainter.state) {
        if (!highResEnabled) {
            showHighResLayer = false
            return@LaunchedEffect
        }
        if (zoomPainter.state is AsyncImagePainter.State.Success) {
            showHighResLayer = true
        }
    }

    LaunchedEffect(safeIndex) {
        scale = 1f
        highResEnabled = false
        showHighResLayer = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .transformable(transformState)
        ) {
            Image(
                painter = basePainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            if (highResEnabled) {
                Image(
                    painter = zoomPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = if (showHighResLayer) 1f else 0f),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
            }
            Text(text = "${safeIndex + 1} / ${imageUris.size}", color = Color.White)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            imageUris.forEachIndexed { index, uri ->
                val thumbnailRequest = rememberMediaImageRequest(
                    storedUri = uri,
                    useCase = MediaImageUseCase.THUMBNAIL
                )
                AsyncImage(
                    model = thumbnailRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onSelect(index) }
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (index == safeIndex) Color(0xFF1DAA90) else Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


