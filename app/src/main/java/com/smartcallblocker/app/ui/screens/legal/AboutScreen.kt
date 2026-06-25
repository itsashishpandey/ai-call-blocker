package com.smartcallblocker.app.ui.screens.legal

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcallblocker.app.BuildConfig
import com.smartcallblocker.app.ui.components.LegalBullet
import com.smartcallblocker.app.ui.components.LegalHeading
import com.smartcallblocker.app.ui.components.LegalParagraph
import com.smartcallblocker.app.ui.components.ScreenTopBar

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    Scaffold(topBar = { ScreenTopBar(title = "About Us", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // Hero card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "AI Spam Call Blocker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Published by ${Publisher.NAME}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            LegalHeading("Our mission")
            LegalParagraph(
                "AI Spam Call Blocker exists to give people back control of their phones. " +
                    "Spam, scams, and repeated harassment calls are an everyday nuisance that wastes time " +
                    "and erodes trust in incoming calls. We believe blocking should be powerful, transparent, " +
                    "and respect your privacy — all without you having to be a power user."
            )

            LegalHeading("How it works")
            LegalParagraph(
                "We use Android's official Call Screening API. When a call comes in, our rule engine " +
                    "decides — within half a second — whether to allow, silence, or reject it based on the " +
                    "rules you set. Everything runs on your device. Your call data never leaves your phone."
            )

            LegalHeading("What makes us different")
            LegalBullet("Offline-first: works without an internet connection.")
            LegalBullet("Zero tracking: no analytics, no ads, no third-party SDKs phoning home.")
            LegalBullet("Granular rules: 20+ rule types from simple prefixes to full regex.")
            LegalBullet("Whitelist priority: trusted numbers always get through.")
            LegalBullet("Smart repeat detection: automatic temporary blocks for harassment.")

            LegalHeading("About ${Publisher.NAME}")
            LegalParagraph(
                "${Publisher.NAME} builds privacy-respecting mobile tools. AI Spam Call Blocker is one of our " +
                    "flagship apps, designed to be uncompromising on user privacy and uncompromisingly powerful " +
                    "for everyday call hygiene."
            )

            LegalHeading("Acknowledgments")
            LegalParagraph(
                "This app uses Google's libphonenumber for accurate phone number parsing across countries, " +
                    "and is built with Jetpack Compose, Room, and Hilt — all open-source libraries from the " +
                    "Android team."
            )

            Spacer(Modifier.height(20.dp))
            ContactRow(
                icon = Icons.Outlined.Language,
                label = Publisher.WEBSITE,
                onClick = { openUrl(ctx, Publisher.WEBSITE) },
            )
            ContactRow(
                icon = Icons.Outlined.Email,
                label = Publisher.SUPPORT_EMAIL,
                onClick = { openEmail(ctx, Publisher.SUPPORT_EMAIL) },
            )
            ContactRow(
                icon = Icons.Outlined.Code,
                label = "Built with Jetpack Compose",
                onClick = null,
            )

            Spacer(Modifier.height(28.dp))
            Text(
                "© 2026 ${Publisher.NAME}. All rights reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .let { if (onClick != null) it.clickableNoRipple(onClick) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

private fun openUrl(ctx: android.content.Context, url: String) {
    runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun openEmail(ctx: android.content.Context, email: String) {
    runCatching {
        ctx.startActivity(
            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                putExtra(Intent.EXTRA_SUBJECT, "AI Spam Call Blocker support")
            },
        )
    }
}
