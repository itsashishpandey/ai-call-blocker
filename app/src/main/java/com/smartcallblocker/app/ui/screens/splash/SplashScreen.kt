package com.smartcallblocker.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcallblocker.app.ui.theme.BrandPrimaryDark
import com.smartcallblocker.app.ui.theme.BrandSecondary
import kotlinx.coroutines.delay

/**
 * The in-app splash. The system splash (Android 12+) shows the launcher icon for ~800ms,
 * then we cross-fade into this screen which animates and routes to the home dashboard.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(400, easing = LinearEasing))
        logoScale.animateTo(1f, tween(550, easing = EaseOutBack))
        titleAlpha.animateTo(1f, tween(450))
        taglineAlpha.animateTo(1f, tween(450))
        delay(600)
        onFinished()
    }

    // subtle pulse ring
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scale",
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "alpha",
    )

    val gradient = Brush.linearGradient(
        colors = listOf(BrandPrimaryDark, BrandSecondary),
        start = Offset(0f, 0f),
        end = Offset.Infinite,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(176.dp)) {
                // pulse ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                )
                // logo circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(96.dp),
                    )
                    Icon(
                        imageVector = Icons.Rounded.Block,
                        contentDescription = null,
                        tint = Color(0xFFFEE2E2),
                        modifier = Modifier
                            .size(34.dp)
                            .padding(top = 4.dp)
                            .align(Alignment.TopEnd),
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Smart Call Blocker",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha.value),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Intelligent Protection",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value),
            )
        }
    }
}
