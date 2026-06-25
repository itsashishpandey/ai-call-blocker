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
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(topBar = { ScreenTopBar(title = "Privacy Policy", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "AI Spam Call Blocker Privacy Policy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            LegalEffectiveDate(Publisher.EFFECTIVE_DATE)

            LegalParagraph(
                "${Publisher.NAME} (\"we\", \"us\", \"our\") respects your privacy. This Privacy Policy " +
                    "explains what information AI Spam Call Blocker (the \"App\") collects, how it is used, " +
                    "and the choices you have. By installing or using the App, you agree to the terms below."
            )

            LegalHeading("1. Our privacy promise")
            LegalParagraph(
                "AI Spam Call Blocker is designed to be offline-first. The App does not transmit your call " +
                    "history, contacts, blocked numbers, or any other personal data to our servers or any " +
                    "third party. Everything you create — rules, whitelist, blacklist, blocked history — " +
                    "stays on your device."
            )

            LegalHeading("2. Information processed on your device")
            LegalParagraph(
                "To deliver call-blocking, the App processes the following information locally on your phone:"
            )
            LegalBullet("Incoming phone numbers (raw and normalised) at the moment a call arrives.")
            LegalBullet("Your contacts, used only to identify whether an incoming number is a known caller.")
            LegalBullet("Recent incoming call events used for repeated-call detection.")
            LegalBullet("Rules, whitelist entries, blacklist entries and settings you create.")
            LegalParagraph(
                "This information is stored exclusively in the App's private storage and is not shared " +
                    "outside the App."
            )

            LegalHeading("3. Permissions and why we ask for them")
            LegalBullet("Phone state — required to be Android's default call-screening app and receive incoming-call events.")
            LegalBullet("Contacts (read-only) — to recognise calls from people in your address book so they aren't accidentally blocked.")
            LegalBullet("Receive Boot Completed — to re-arm time-limited (temporary) blocks after the device restarts.")
            LegalParagraph(
                "We never use these permissions for advertising, profiling, marketing, analytics, or " +
                    "any purpose other than call screening as described here."
            )

            LegalHeading("4. Data we do NOT collect")
            LegalBullet("We do not collect personally identifying information such as your name, email or device ID.")
            LegalBullet("We do not use third-party analytics, advertising SDKs, or crash-reporting services.")
            LegalBullet("We do not share, sell, rent, or trade data with anyone.")
            LegalBullet("We do not track your location.")

            LegalHeading("5. Data retention and deletion")
            LegalParagraph(
                "Blocked-call history, rules, whitelist and blacklist remain on your device until you " +
                    "delete them in-app or uninstall the App. You can also configure automatic deletion " +
                    "of old blocked-call logs from Settings. Uninstalling the App permanently removes " +
                    "all data the App stored."
            )

            LegalHeading("6. Children's privacy")
            LegalParagraph(
                "The App is intended for general audiences and is not directed at children under 13. We " +
                    "do not knowingly collect personal information from children."
            )

            LegalHeading("7. Security")
            LegalParagraph(
                "Because we store data only on your device, your call data is protected by the same " +
                    "platform sandboxing and (optionally) device encryption Android provides for all apps. " +
                    "You may also enable App Lock in Settings to require biometrics before opening the App."
            )

            LegalHeading("8. Backups")
            LegalParagraph(
                "If you enable Android's automatic backup, the App's settings and database may be included " +
                    "in your device's encrypted Google Drive backup, as managed by Android itself. We do not " +
                    "have access to those backups."
            )

            LegalHeading("9. Third-party libraries")
            LegalParagraph(
                "The App uses open-source libraries to function, including Google libphonenumber for number " +
                    "parsing. These libraries operate entirely on-device and do not transmit data."
            )

            LegalHeading("10. Changes to this policy")
            LegalParagraph(
                "We may update this Privacy Policy from time to time. The latest version will always be " +
                    "available within the App and at ${Publisher.WEBSITE}. Significant changes will be " +
                    "highlighted with a new effective date."
            )

            LegalHeading("11. Contact us")
            LegalParagraph(
                "Questions about this Privacy Policy? Reach us at ${Publisher.PRIVACY_EMAIL} or visit " +
                    "${Publisher.WEBSITE}."
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}
