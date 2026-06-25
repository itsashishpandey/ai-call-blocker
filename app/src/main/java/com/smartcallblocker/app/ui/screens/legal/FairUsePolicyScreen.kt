package com.smartcallblocker.app.ui.screens.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartcallblocker.app.ui.components.LegalBullet
import com.smartcallblocker.app.ui.components.LegalEffectiveDate
import com.smartcallblocker.app.ui.components.LegalHeading
import com.smartcallblocker.app.ui.components.LegalParagraph
import com.smartcallblocker.app.ui.components.ScreenTopBar

@Composable
fun FairUsePolicyScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ScreenTopBar(title = "Fair Use Policy", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Fair Use Policy & Terms",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            LegalEffectiveDate(Publisher.EFFECTIVE_DATE)

            LegalParagraph(
                "Welcome to Smart Call Blocker. This Fair Use Policy describes how the App may be used " +
                    "and the terms that govern your use. By installing or continuing to use the App, you " +
                    "agree to these terms."
            )

            LegalHeading("1. Licence")
            LegalParagraph(
                "${Publisher.NAME} grants you a personal, non-exclusive, non-transferable, revocable " +
                    "licence to install and use the App on Android devices you own or control, for your " +
                    "own personal, non-commercial purposes."
            )

            LegalHeading("2. Acceptable use")
            LegalParagraph(
                "The App is intended for personal call hygiene — to block spam, scams, and unwanted callers, " +
                    "and to give important contacts priority. We expect you to use it responsibly:"
            )
            LegalBullet("Use the App for your own incoming calls on devices you control.")
            LegalBullet("Manage your whitelist and blacklist in good faith.")
            LegalBullet("Review your rules periodically so legitimate contacts aren't accidentally blocked.")

            LegalHeading("3. Prohibited use")
            LegalParagraph("You agree NOT to use the App to:")
            LegalBullet("Attempt to block emergency services, helplines, or lawful government notifications. The App is designed to always allow recognised emergency numbers regardless of rules, and you must not attempt to circumvent this behaviour.")
            LegalBullet("Harass, intimidate, or defame any person — for example, by adding numbers to the blacklist solely to cause them harm.")
            LegalBullet("Reverse-engineer, decompile, disassemble, or attempt to derive the source code of the App, except as expressly permitted by law.")
            LegalBullet("Remove or modify any proprietary notices, branding, or attribution within the App.")
            LegalBullet("Distribute, sublicense, sell, rent, or lease the App or any part of it.")
            LegalBullet("Use the App in any manner that violates applicable laws, including telecommunications, privacy, or anti-stalking laws in your jurisdiction.")

            LegalHeading("4. Reasonable use limits")
            LegalParagraph(
                "The App is designed to scale to large rule sets and call volumes, but performance may " +
                    "degrade if you create an unreasonably large number of rules (for example, more than " +
                    "100,000) or extremely complex regular expressions. Such use is outside the scope of " +
                    "fair personal use and we are not responsible for performance issues that result."
            )

            LegalHeading("5. Call screening behaviour")
            LegalParagraph(
                "Final call-screening decisions are made by the Android operating system based on the " +
                    "rules you configure. The App applies your rules within the strict timing budget Android " +
                    "imposes on screening apps. Edge cases — including network-side spam tagging, dual-SIM " +
                    "behaviour, VoLTE/VoWiFi quirks, and carrier-specific call presentation — may cause " +
                    "occasional misclassification. The App is not a substitute for safety-critical filtering."
            )

            LegalHeading("6. No warranty")
            LegalParagraph(
                "The App is provided \"AS IS\" and \"AS AVAILABLE\" without warranties of any kind, express " +
                    "or implied, including but not limited to merchantability, fitness for a particular " +
                    "purpose, and non-infringement. We do not warrant that the App will be uninterrupted, " +
                    "error-free, or that every unwanted call will be blocked."
            )

            LegalHeading("7. Limitation of liability")
            LegalParagraph(
                "To the maximum extent permitted by law, ${Publisher.NAME} shall not be liable for any " +
                    "indirect, incidental, special, consequential, or punitive damages, or any loss of " +
                    "profits or revenue, whether incurred directly or indirectly, arising from your use " +
                    "of, or inability to use, the App — including missed calls due to over-aggressive " +
                    "rules you create."
            )

            LegalHeading("8. Termination")
            LegalParagraph(
                "We may suspend or terminate your licence to use the App at any time if you breach this " +
                    "Fair Use Policy. You may stop using the App at any time by uninstalling it, which " +
                    "will permanently remove all data the App stored on your device."
            )

            LegalHeading("9. Updates")
            LegalParagraph(
                "We may update the App from time to time to add features, fix bugs, improve performance, " +
                    "or comply with platform requirements. By keeping the App installed you consent to " +
                    "receiving updates through Google Play."
            )

            LegalHeading("10. Changes to this policy")
            LegalParagraph(
                "We may update this Fair Use Policy. Material changes will be reflected in a new effective " +
                    "date and surfaced within the App. Continued use after a change indicates acceptance."
            )

            LegalHeading("11. Governing law")
            LegalParagraph(
                "This policy is governed by the laws of the jurisdiction in which ${Publisher.NAME} is " +
                    "established, without regard to its conflict of law principles."
            )

            LegalHeading("12. Contact us")
            LegalParagraph(
                "Questions about this Fair Use Policy can be sent to ${Publisher.SUPPORT_EMAIL} or via " +
                    "${Publisher.WEBSITE}."
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}
