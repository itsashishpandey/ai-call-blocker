package com.smartcallblocker.app.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.ui.components.ScreenTopBar
import com.smartcallblocker.app.ui.theme.StatusAllowed
import com.smartcallblocker.app.ui.theme.StatusBlocked
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    vm: BackupViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val pickFolder = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) vm.onFolderPicked(uri)
    }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSnackbar()
        }
    }

    Scaffold(
        topBar = { ScreenTopBar(title = "Backup & Restore", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ConnectionCard(
                folderUri = state.folderUri,
                lastBackupTime = state.lastBackupTime,
                lastStatus = state.lastStatus,
                isWorking = state.isWorking,
                onPickFolder = { pickFolder.launch(null) },
                onDisconnect = { vm.disconnect() },
            )

            if (state.folderUri != null) {
                AutoBackupCard(
                    enabled = state.autoBackupEnabled,
                    onToggle = vm::setAutoBackup,
                )

                ActionsCard(
                    isWorking = state.isWorking,
                    onBackupNow = vm::backupNow,
                    onRestoreNow = vm::restoreNow,
                )
            }

            InfoCard()

            Spacer(Modifier.height(24.dp))
        }
    }

    if (state.showRestorePrompt) {
        RestoreConfirmDialog(
            timestamp = state.pendingProbe?.lastModified ?: 0L,
            sizeBytes = state.pendingProbe?.sizeBytes ?: 0L,
            onConfirm = vm::confirmRestore,
            onDecline = vm::declineRestore,
            onDismiss = vm::dismissRestorePrompt,
        )
    }
}

@Composable
private fun ConnectionCard(
    folderUri: String?,
    lastBackupTime: Long,
    lastStatus: String,
    isWorking: Boolean,
    onPickFolder: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val connected = folderUri != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (connected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (connected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (connected) Icons.Rounded.CheckCircle else Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (connected) "Connected" else "Not set up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (connected) folderDisplayName(folderUri!!)
                        else "Pick a folder to start saving your data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isWorking) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }

            if (connected && lastBackupTime > 0) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Sync, contentDescription = null,
                        tint = StatusAllowed,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Last backup: ${formatRelative(lastBackupTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (lastStatus.isNotBlank()) {
                    Text(
                        lastStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            if (connected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isWorking,
                ) {
                    Icon(Icons.Outlined.CloudOff, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Disconnect folder")
                }
            } else {
                Button(
                    onClick = onPickFolder,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isWorking,
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pick backup folder")
                }
            }
        }
    }
}

@Composable
private fun AutoBackupCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Sync, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto-save", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Save every change to your backup folder within a second",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ActionsCard(
    isWorking: Boolean,
    onBackupNow: () -> Unit,
    onRestoreNow: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Manual actions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = onBackupNow,
                    enabled = !isWorking,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Back up now")
                }
                FilledTonalButton(
                    onClick = onRestoreNow,
                    enabled = !isWorking,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Restore")
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Text("How it works", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Pick any folder on your phone (we recommend Documents → SmartCallBlocker). " +
                    "Every time you add a rule, block a number, or change a setting, the app " +
                    "writes a single backup.json file to that folder within a second.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "After you reinstall the app, come back here, tap \"Pick backup folder\", " +
                    "choose the same folder, and confirm \"Restore\". Everything comes back.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    timestamp: Long,
    sizeBytes: Long,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Existing backup found") },
        text = {
            Column {
                Text(
                    "We found a Smart Call Blocker backup in this folder. Do you want to " +
                        "restore it now? This will replace all current rules, blacklist, " +
                        "whitelist, blocked-call history and settings with the backup.",
                )
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Created: ${formatFull(timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Size: ${formatBytes(sizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text("Use this folder for new backups", color = StatusBlocked)
            }
        },
    )
}

private fun folderDisplayName(uri: String): String {
    val decoded = java.net.URLDecoder.decode(uri, "UTF-8")
    val parts = decoded.substringAfter("tree/", "").split(":", "/")
    val tail = parts.lastOrNull { it.isNotBlank() } ?: ""
    return if (tail.isBlank()) "Selected folder" else tail
}

private fun formatRelative(ts: Long): String {
    if (ts <= 0) return "never"
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000} min ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hr ago"
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))
    }
}

private fun formatFull(ts: Long): String {
    if (ts <= 0) return "unknown"
    return SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(ts))
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}
