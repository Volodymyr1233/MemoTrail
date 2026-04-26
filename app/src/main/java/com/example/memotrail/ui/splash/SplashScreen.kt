package com.example.memotrail.ui.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrail.R
import com.example.memotrail.ui.theme.ForestGreen
import com.example.memotrail.ui.theme.SoftMint
import com.example.memotrail.ui.theme.SoftOrange
import com.example.memotrail.ui.theme.TealAccent

import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    val transition = rememberInfiniteTransition(label = "dots")
    val dot1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, delayMillis = 0), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, delayMillis = 180), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, delayMillis = 360), RepeatMode.Reverse),
        label = "dot3"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.loading_img), // Replace with your actual PNG name
            contentDescription = stringResource(R.string.memotrail_logo_content_description),
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Crop 
        )

        Text(
            text = stringResource(R.string.app_name),
            style = TextStyle(
                fontWeight = FontWeight.W700,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TealAccent, SoftOrange
                    )
                )),
            fontSize = 36.sp
            )

        Text(
            text = stringResource(R.string.splash_tagline),
            style = TextStyle(
                fontWeight = FontWeight.W400,
               ),
            fontSize = 14.sp
        )

        Row(modifier = Modifier.padding(top = 40.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedDot(alpha = dot1)
            AnimatedDot(alpha = dot2)
            AnimatedDot(alpha = dot3)
        }
    }
}

@Composable
private fun AnimatedDot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        onTimeout = {}
    )
}


