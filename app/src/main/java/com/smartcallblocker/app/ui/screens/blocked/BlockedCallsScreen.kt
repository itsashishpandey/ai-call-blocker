package com.smartcallblocker.app.ui.screens.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.R
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.ui.components.EmptyState
import com.smartcallblocker.app.ui.components.ScreenTopBar
import com.smartcallblocker.app.util.PhoneNumberMasker
import com.smartcallblocker.app.ui.theme.StatusBlocked
import com.smartcallblocker.app.ui.theme.StatusSilenced
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlockedCallsScreen(
    onBack: () -> Unit,
    vm: BlockedCallsViewModel = hiltViewModel(),
) {
    val calls by vm.calls.collectAsState()
    val privacyMode by vm.privacyMode.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.nav_blocked),
                onBack = onBack,
                actions = {
                    if (calls.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear all")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (calls.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = stringResource(R.string.empty_blocked),
                description = stringResource(R.string.empty_blocked_desc),
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(calls, key = { it.id }) { call ->
                    BlockedCallItem(call, privacyMode, onDelete = { vm.delete(call.id) })
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all blocked call history?") },
            text = { Text("This will permanently delete every entry. It cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll(); confirmClear = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun BlockedCallItem(call: BlockedCallEntity, privacyMode: Boolean, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tint = if (call.action == "SILENCED") StatusSilenced else StatusBlocked
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (call.action == "SILENCED") Icons.Rounded.VolumeOff else Icons.Rounded.CallEnd,
                    contentDescription = null,
                    tint = tint,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayName = if (call.callerName != null) {
                    PhoneNumberMasker.maskName(call.callerName, privacyMode)
                } else {
                    val raw = call.normalizedNumber.ifEmpty { call.phoneNumber.ifEmpty { "" } }
                    PhoneNumberMasker.mask(raw, privacyMode).ifEmpty { "Private/Unknown" }
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
                Text(
                    text = formatDateTime(call.callDateTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatDateTime(ts: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))
