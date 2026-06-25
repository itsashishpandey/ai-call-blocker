package com.smartcallblocker.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.BuildConfig
import com.smartcallblocker.app.R
import com.smartcallblocker.app.data.preferences.ThemeMode
import com.smartcallblocker.app.domain.model.BlockAction
import com.smartcallblocker.app.ui.components.ScreenTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenFairUse: () -> Unit,
    onOpenBackup: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = { ScreenTopBar(title = stringResource(R.string.nav_settings), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Protection") {
                SettingSwitch(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.setting_enable_protection),
                    subtitle = "Master toggle for all call blocking",
                    checked = s.protectionEnabled,
                    onCheckedChange = vm::setProtection,
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.setting_action_type), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = s.action == BlockAction.REJECT,
                        onClick = { vm.setAction(BlockAction.REJECT) },
                        label = { Text(stringResource(R.string.setting_action_reject)) },
                    )
                    FilterChip(
                        selected = s.action == BlockAction.SILENCE,
                        onClick = { vm.setAction(BlockAction.SILENCE) },
                        label = { Text(stringResource(R.string.setting_action_silence)) },
                    )
                    FilterChip(
                        selected = s.action == BlockAction.DISALLOW,
                        onClick = { vm.setAction(BlockAction.DISALLOW) },
                        label = { Text("Voicemail") },
                    )
                }
            }

            SectionCard(title = "Carrier spam detection") {
                SettingSwitch(
                    icon = Icons.Outlined.GppMaybe,
                    title = "Block calls flagged as spam by carrier",
                    subtitle = "Auto-reject calls your network has marked as Suspected Spam or that fail caller-ID verification",
                    checked = s.blockCarrierSpam,
                    onCheckedChange = vm::setBlockCarrierSpam,
                )
                CarrierSpamDisclaimer()
            }

            SectionCard(title = "Repeated calls") {
                SettingSwitch(
                    icon = Icons.Outlined.Repeat,
                    title = "Auto-block repeated calls",
                    subtitle = "If a number calls too many times in a short window, temporarily block it.",
                    checked = s.repeatedEnabled,
                    onCheckedChange = vm::setRepeatedEnabled,
                )
                SliderSetting(
                    label = "Calls before block",
                    value = s.repeatedLimit.toFloat(),
                    range = 2f..10f,
                    valueFormatter = { "${it.toInt()} calls" },
                    onChange = { vm.setRepeatedLimit(it.toInt()) },
                )
                SliderSetting(
                    label = "Within window",
                    value = s.repeatedWindowMin.toFloat(),
                    range = 1f..30f,
                    valueFormatter = { "${it.toInt()} min" },
                    onChange = { vm.setRepeatedWindow(it.toInt()) },
                )
                SliderSetting(
                    label = "Temporary block duration",
                    value = s.repeatedBlockMin.toFloat(),
                    range = 5f..180f,
                    valueFormatter = { "${it.toInt()} min" },
                    onChange = { vm.setRepeatedBlock(it.toInt()) },
                )
            }

            SectionCard(title = "Notifications & logs") {
                SettingSwitch(
                    icon = Icons.Outlined.NotificationsOff,
                    title = stringResource(R.string.setting_skip_notification),
                    subtitle = "Don't show a notification for blocked calls",
                    checked = s.skipNotification,
                    onCheckedChange = vm::setSkipNotification,
                )
                SettingSwitch(
                    icon = Icons.Outlined.VolumeOff,
                    title = stringResource(R.string.setting_skip_call_log),
                    subtitle = "Don't add blocked calls to the system call log",
                    checked = s.skipCallLog,
                    onCheckedChange = vm::setSkipCallLog,
                )
                SettingSwitch(
                    icon = Icons.Outlined.Description,
                    title = stringResource(R.string.setting_save_logs),
                    subtitle = "Show blocked calls history in this app",
                    checked = s.saveLogs,
                    onCheckedChange = vm::setSaveLogs,
                )
            }

            SectionCard(title = "Appearance") {
                Text(stringResource(R.string.setting_dark_mode), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = s.themeMode == ThemeMode.SYSTEM,
                        onClick = { vm.setThemeMode(ThemeMode.SYSTEM) },
                        label = { Text(stringResource(R.string.setting_dark_system)) },
                    )
                    FilterChip(
                        selected = s.themeMode == ThemeMode.LIGHT,
                        onClick = { vm.setThemeMode(ThemeMode.LIGHT) },
                        label = { Text(stringResource(R.string.setting_dark_light)) },
                    )
                    FilterChip(
                        selected = s.themeMode == ThemeMode.DARK,
                        onClick = { vm.setThemeMode(ThemeMode.DARK) },
                        label = { Text(stringResource(R.string.setting_dark_dark)) },
                    )
                }
            }

            SectionCard(title = "Privacy") {
                SettingSwitch(
                    icon = Icons.Outlined.VisibilityOff,
                    title = stringResource(R.string.setting_privacy_mode),
                    subtitle = "Mask numbers in blocked history",
                    checked = s.privacyMode,
                    onCheckedChange = vm::setPrivacyMode,
                )
                SettingSwitch(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.setting_app_lock),
                    subtitle = "Require biometrics to open the app",
                    checked = s.appLock,
                    onCheckedChange = vm::setAppLock,
                )
                SliderSetting(
                    label = stringResource(R.string.setting_auto_delete),
                    value = s.autoDeleteDays.toFloat(),
                    range = 7f..365f,
                    valueFormatter = { "${it.toInt()} days" },
                    onChange = { vm.setAutoDeleteDays(it.toInt()) },
                )
            }

            SectionCard(title = "Backup & Restore") {
                LinkRow(
                    icon = Icons.Outlined.CloudSync,
                    title = "Backup & Restore",
                    subtitle = "Auto-save your data to a folder; restore after reinstall",
                    onClick = onOpenBackup,
                )
            }

            SectionCard(title = "About & legal") {
                LinkRow(
                    icon = Icons.Outlined.Info,
                    title = "About Us",
                    subtitle = "Learn about the app and the team",
                    onClick = onOpenAbout,
                )
                LinkRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How your data is handled",
                    onClick = onOpenPrivacy,
                )
                LinkRow(
                    icon = Icons.Outlined.Gavel,
                    title = "Fair Use Policy",
                    subtitle = "Acceptable use and terms",
                    onClick = onOpenFairUse,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Article, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "Version ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CarrierSpamDisclaimer() {
    Spacer(Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "How carrier spam detection works",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Smart Call Blocker reads the verification signal Android exposes for each incoming call " +
                            "(STIR/SHAKEN). When your carrier marks a call as failed verification — the same signal " +
                            "that makes your dialer show \"Suspected Spam\" with a red banner — we'll reject it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Support varies by device, carrier, and country:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Best on Pixel, Samsung, OnePlus, and recent Xiaomi devices in regions where the dialer's " +
                            "built-in spam database is active.\n" +
                            "• Requires Android 11 or newer.\n" +
                            "• Keep your phone dialer's own \"Caller ID & spam protection\" turned on for best results.\n" +
                            "• The spam database used by Google Phone or Samsung Smart Call is private to that app — " +
                            "no third-party app, including this one, can read it directly. We act on what Android tells us.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String,
    onChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(valueFormatter(value), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
        )
    }
}
