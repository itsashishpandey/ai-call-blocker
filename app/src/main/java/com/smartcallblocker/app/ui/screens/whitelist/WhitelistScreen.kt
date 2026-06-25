package com.smartcallblocker.app.ui.screens.whitelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.smartcallblocker.app.data.db.entities.WhitelistEntity
import com.smartcallblocker.app.ui.components.EmptyState
import com.smartcallblocker.app.ui.components.ScreenTopBar
import com.smartcallblocker.app.ui.theme.StatusAllowed

@Composable
fun WhitelistScreen(
    onBack: () -> Unit,
    vm: WhitelistViewModel = hiltViewModel(),
) {
    val items by vm.items.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.nav_whitelist),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { vm.importContacts() }) {
                        Icon(Icons.Rounded.ContactPhone, contentDescription = "Import contacts")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Rounded.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add)) },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.PlaylistAddCheck,
                title = stringResource(R.string.empty_whitelist),
                description = stringResource(R.string.empty_whitelist_desc),
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.id }) { entry ->
                    WhitelistItem(entry, onDelete = { vm.delete(entry.id) })
                }
            }
        }
    }

    if (showAdd) {
        AddWhitelistDialog(
            onDismiss = { showAdd = false },
            onConfirm = { phone, name ->
                vm.add(phone, name); showAdd = false
            },
        )
    }
}

@Composable
private fun WhitelistItem(entry: WhitelistEntity, onDelete: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(StatusAllowed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.PlaylistAddCheck, contentDescription = null, tint = StatusAllowed)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name ?: entry.phoneNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = entry.normalizedNumber.ifEmpty { entry.phoneNumber },
                    style = MaterialTheme.typography.bodySmall,
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

@Composable
private fun AddWhitelistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to whitelist") },
        text = {
            Column {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone number") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.padding(top = 12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(phone, name.takeIf { it.isNotBlank() }) }, enabled = phone.isNotBlank()) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
