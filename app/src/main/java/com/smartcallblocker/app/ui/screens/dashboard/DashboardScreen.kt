package com.smartcallblocker.app.ui.screens.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneCallback
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.smartcallblocker.app.R
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.service.ScreeningRoleManager
import com.smartcallblocker.app.ui.theme.StatusAllowed
import com.smartcallblocker.app.ui.theme.StatusBlocked
import com.smartcallblocker.app.ui.theme.StatusPending
import com.smartcallblocker.app.ui.theme.StatusSilenced
import com.smartcallblocker.app.util.PhoneNumberMasker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenBlocked: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenWhitelist: () -> Unit,
    onOpenBlacklist: () -> Unit,
    onAddRule: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Refresh on every resume so permissions/role grants are immediately reflected.
    var needsPermissions by remember { mutableStateOf(checkMissingPermissions(context)) }
    var isDefaultScreener by remember { mutableStateOf(ScreeningRoleManager.isHeld(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        needsPermissions = checkMissingPermissions(context)
        isDefaultScreener = ScreeningRoleManager.isHeld(context)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { needsPermissions = checkMissingPermissions(context) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { isDefaultScreener = ScreeningRoleManager.isHeld(context) }

    var showSafeModeSheet by remember { mutableStateOf(false) }
    var showLockdownSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.nav_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Lockdown banner takes priority over safe-mode banner — they're mutually exclusive.
            if (state.lockdownExpiry > System.currentTimeMillis()) {
                LockdownBanner(
                    expiry = state.lockdownExpiry,
                    onCancel = vm::cancelLockdown,
                )
            } else if (state.safeModeExpiry > System.currentTimeMillis()) {
                SafeModeBanner(
                    expiry = state.safeModeExpiry,
                    onCancel = vm::cancelSafeMode,
                )
            }

            ProtectionStatusCard(
                enabled = state.protectionEnabled,
                isDefaultScreener = isDefaultScreener,
                onToggle = vm::toggleProtection,
                onMakeDefault = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity != null) {
                        val intent = ScreeningRoleManager.requestIntent(activity)
                        if (intent != null) roleLauncher.launch(intent)
                    }
                },
                onSafeMode = { showSafeModeSheet = true },
            )

            if (needsPermissions.isNotEmpty()) {
                PermissionsBanner(onGrant = { permLauncher.launch(needsPermissions.toTypedArray()) })
            }

            QuickTogglesCard(
                blockAllUnknown = state.blockAllUnknown,
                blockLandline = state.blockLandline,
                blockTollFree = state.blockTollFree,
                lockdownActive = state.lockdownExpiry > System.currentTimeMillis(),
                onBlockAllUnknown = vm::setBlockAllUnknown,
                onBlockLandline = vm::setBlockLandline,
                onBlockTollFree = vm::setBlockTollFree,
                onLockdown = { showLockdownSheet = true },
                onCancelLockdown = vm::cancelLockdown,
            )

            StatsGrid(state, onOpenStatistics)

            QuickActions(
                onAddRule = onAddRule,
                onOpenWhitelist = onOpenWhitelist,
                onOpenBlacklist = onOpenBlacklist,
                onOpenRules = onOpenRules,
            )

            LastBlockedCard(state.lastBlocked, state.privacyMode, onOpenHistory = onOpenBlocked)

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showSafeModeSheet) {
        SafeModeDialog(
            onDismiss = { showSafeModeSheet = false },
            onStart = { minutes ->
                vm.startSafeMode(minutes)
                showSafeModeSheet = false
            },
        )
    }

    if (showLockdownSheet) {
        LockdownDialog(
            onDismiss = { showLockdownSheet = false },
            onStart = { minutes ->
                vm.startLockdown(minutes)
                showLockdownSheet = false
            },
        )
    }
}

@Composable
private fun SafeModeBanner(expiry: Long, onCancel: () -> Unit) {
    val remainingMin = ((expiry - System.currentTimeMillis()) / 60_000L).coerceAtLeast(0).toInt()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StatusSilenced.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Timer, contentDescription = null, tint = StatusSilenced)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Safe mode is on", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "All calls allowed for the next $remainingMin min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun LockdownBanner(expiry: Long, onCancel: () -> Unit) {
    val remainingMin = ((expiry - System.currentTimeMillis()) / 60_000L).coerceAtLeast(0).toInt()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StatusBlocked.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.DoNotDisturbOn, contentDescription = null, tint = StatusBlocked)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Blocking all calls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Every call is rejected for the next $remainingMin min · whitelist still allowed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onCancel) { Text("Stop") }
        }
    }
}

@Composable
private fun QuickTogglesCard(
    blockAllUnknown: Boolean,
    blockLandline: Boolean,
    blockTollFree: Boolean,
    lockdownActive: Boolean,
    onBlockAllUnknown: (Boolean) -> Unit,
    onBlockLandline: (Boolean) -> Unit,
    onBlockTollFree: (Boolean) -> Unit,
    onLockdown: () -> Unit,
    onCancelLockdown: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Quick toggles", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Saved contacts are never blocked by these toggles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            ToggleRow(
                icon = Icons.Rounded.Help,
                tint = StatusBlocked,
                title = "Block all unknown calls",
                subtitle = "Reject any number not in your contacts",
                checked = blockAllUnknown,
                onChange = onBlockAllUnknown,
            )
            ToggleRow(
                icon = Icons.Rounded.Phone,
                tint = StatusSilenced,
                title = "Block landline calls",
                subtitle = "Reject fixed-line numbers detected via carrier metadata",
                checked = blockLandline,
                onChange = onBlockLandline,
            )
            ToggleRow(
                icon = Icons.Rounded.PhoneCallback,
                tint = StatusPending,
                title = "Block toll-free calls",
                subtitle = "Reject 1-800, 1-888, 0-800, 1800-series and similar",
                checked = blockTollFree,
                onChange = onBlockTollFree,
            )

            Spacer(Modifier.height(4.dp))
            if (lockdownActive) {
                FilledTonalButton(
                    onClick = onCancelLockdown,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.DoNotDisturbOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop blocking all calls")
                }
            } else {
                FilledTonalButton(
                    onClick = onLockdown,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.DoNotDisturbOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Block all calls for X minutes")
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun LockdownDialog(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    var custom by remember { mutableStateOf("60") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block all calls for…") },
        text = {
            Column {
                Text(
                    "Every incoming call will be rejected. Emergency numbers and contacts on your " +
                        "whitelist will still come through.",
                )
                Spacer(Modifier.height(12.dp))
                Text("Pick a duration:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 60, 120).forEach { mins ->
                        AssistChip(
                            onClick = { onStart(mins) },
                            label = { Text("$mins min") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = custom,
                    onValueChange = { input -> custom = input.filter { it.isDigit() }.take(4) },
                    label = { Text("Custom duration (1–1440 minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = custom.toIntOrNull()?.coerceIn(1, 1440) ?: 60
                    onStart(mins)
                },
                enabled = custom.toIntOrNull()?.let { it in 1..1440 } == true,
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SafeModeDialog(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start safe mode") },
        text = {
            Column {
                Text(
                    "Allow every incoming call for a short period — useful when you're expecting an " +
                        "important call from an unknown number.",
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 60).forEach { mins ->
                        AssistChip(
                            onClick = { onStart(mins) },
                            label = { Text("$mins min") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun ProtectionStatusCard(
    enabled: Boolean,
    isDefaultScreener: Boolean,
    onToggle: (Boolean) -> Unit,
    onMakeDefault: () -> Unit,
    onSafeMode: () -> Unit,
) {
    val active = enabled && isDefaultScreener
    val containerColor = if (active)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = if (active) 1f else 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (active) Icons.Rounded.Shield else Icons.Rounded.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (active)
                            stringResource(R.string.protection_active)
                        else
                            stringResource(R.string.protection_inactive),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (active)
                            "All rules are enforced. You're protected."
                        else if (!isDefaultScreener)
                            "Set this app as default screening app"
                        else
                            "Toggle protection back on to resume blocking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            if (!isDefaultScreener) {
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onMakeDefault,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.set_as_default))
                }
            } else if (active) {
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onSafeMode,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start safe mode")
                }
            }
        }
    }
}

@Composable
private fun PermissionsBanner(onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.permissions_required), style = MaterialTheme.typography.titleMedium)
                Text(
                    "Phone and contacts access are required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(onClick = onGrant) {
                Text(stringResource(R.string.grant_permissions))
            }
        }
    }
}

@Composable
private fun StatsGrid(state: DashboardUiState, onOpenStatistics: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "At a glance",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onOpenStatistics) {
                Icon(Icons.Outlined.Insights, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Statistics")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.smartcallblocker.app.ui.components.StatCard(
                title = stringResource(R.string.blocked_today),
                value = state.blockedToday.toString(),
                icon = Icons.Rounded.Today,
                tint = StatusBlocked,
                modifier = Modifier.weight(1f),
            )
            com.smartcallblocker.app.ui.components.StatCard(
                title = stringResource(R.string.blocked_week),
                value = state.blockedWeek.toString(),
                icon = Icons.Rounded.ViewWeek,
                tint = StatusSilenced,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.smartcallblocker.app.ui.components.StatCard(
                title = stringResource(R.string.blocked_month),
                value = state.blockedMonth.toString(),
                icon = Icons.Rounded.CalendarMonth,
                tint = StatusPending,
                modifier = Modifier.weight(1f),
            )
            com.smartcallblocker.app.ui.components.StatCard(
                title = stringResource(R.string.active_rules),
                value = state.activeRules.toString(),
                icon = Icons.AutoMirrored.Rounded.Rule,
                tint = StatusAllowed,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActions(
    onAddRule: () -> Unit,
    onOpenWhitelist: () -> Unit,
    onOpenBlacklist: () -> Unit,
    onOpenRules: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.quick_actions),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    label = stringResource(R.string.add_rule),
                    icon = Icons.Rounded.Add,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onAddRule,
                    modifier = Modifier.weight(1f),
                )
                QuickActionButton(
                    label = stringResource(R.string.nav_rules),
                    icon = Icons.Outlined.Tune,
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenRules,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    label = stringResource(R.string.nav_whitelist),
                    icon = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
                    tint = StatusAllowed,
                    onClick = onOpenWhitelist,
                    modifier = Modifier.weight(1f),
                )
                QuickActionButton(
                    label = stringResource(R.string.nav_blacklist),
                    icon = Icons.Outlined.Block,
                    tint = StatusBlocked,
                    onClick = onOpenBlacklist,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 14.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LastBlockedCard(call: BlockedCallEntity?, privacyMode: Boolean, onOpenHistory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.last_blocked),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalButton(onClick = onOpenHistory, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Rounded.History, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.view_history))
                }
            }
            Spacer(Modifier.height(12.dp))
            if (call == null) {
                Text(
                    stringResource(R.string.empty_blocked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StatusBlocked.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.CallEnd, contentDescription = null, tint = StatusBlocked)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val displayName = if (call.callerName != null) {
                            PhoneNumberMasker.maskName(call.callerName, privacyMode)
                        } else {
                            val raw = call.normalizedNumber.ifEmpty { call.phoneNumber.ifEmpty { "" } }
                            PhoneNumberMasker.mask(raw, privacyMode).ifEmpty { "Unknown" }
                        }
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = call.matchedRuleName ?: call.notes ?: "Blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(call.callDateTime)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun checkMissingPermissions(context: Context): List<String> {
    val needed = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
    )
    return needed.filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }
}
