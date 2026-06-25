package com.smartcallblocker.app.ui.screens.onboarding

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.service.ScreeningRoleManager
import com.smartcallblocker.app.ui.theme.BrandPrimaryDark
import com.smartcallblocker.app.ui.theme.BrandSecondary
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val pages = listOf(
    OnboardingPage(
        Icons.Rounded.Shield,
        "Welcome to AI Spam Call Blocker",
        "Take back control of your phone. Block spam, scams, and repeated harassment calls — while important calls still get through.",
    ),
    OnboardingPage(
        Icons.Rounded.Phone,
        "We need a few permissions",
        "To screen incoming calls and recognise people you know, we need access to your phone state and contacts. Everything stays on your device — we never send your data anywhere.",
    ),
    OnboardingPage(
        Icons.Rounded.AdminPanelSettings,
        "Become the default screening app",
        "Android requires one screening app at a time. Set AI Spam Call Blocker as default to enable blocking. You can change this any time from system settings.",
    ),
    OnboardingPage(
        Icons.Rounded.CheckCircle,
        "You're all set",
        "Create rules, manage your whitelist, and we'll handle the rest. Browse the dashboard for stats and tap a rule to customise it.",
    ),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    val finishing by vm.finishing.collectAsState()

    LaunchedEffect(finishing) { if (finishing) onFinished() }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* re-check happens via lifecycle */ }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { /* user returns */ }

    val gradient = Brush.linearGradient(
        colors = listOf(BrandPrimaryDark, BrandSecondary),
        start = Offset(0f, 0f),
        end = Offset.Infinite,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 32.dp),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateFloatAsState(
                        targetValue = if (selected) 22f else 8f,
                        animationSpec = tween(220, easing = LinearEasing),
                        label = "indicator",
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = width.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color.White
                                else Color.White.copy(alpha = 0.35f),
                            ),
                    )
                }
            }

            // Action buttons — explicit white styling so they're readable on the gradient.
            val outlinedColors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
            )
            val outlinedBorder = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors,
                        border = outlinedBorder,
                    ) {
                        Text("Back", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                } else {
                    OutlinedButton(
                        onClick = vm::finish,
                        modifier = Modifier.weight(1f),
                        colors = outlinedColors,
                        border = outlinedBorder,
                    ) {
                        Text("Skip", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
                Button(
                    onClick = {
                        when (pagerState.currentPage) {
                            1 -> {
                                val needed = mutableListOf(
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.READ_CONTACTS,
                                )
                                permLauncher.launch(needed.toTypedArray())
                                scope.launch { pagerState.animateScrollToPage(2) }
                            }
                            2 -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity != null) {
                                    val intent = ScreeningRoleManager.requestIntent(activity)
                                    if (intent != null) roleLauncher.launch(intent)
                                }
                                scope.launch { pagerState.animateScrollToPage(3) }
                            }
                            pages.size - 1 -> vm.finish()
                            else -> scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BrandPrimaryDark,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        disabledContentColor = BrandPrimaryDark.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        when (pagerState.currentPage) {
                            1 -> "Grant"
                            2 -> "Set Default"
                            pages.size - 1 -> "Get Started"
                            else -> "Next"
                        },
                        color = BrandPrimaryDark,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.description,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

