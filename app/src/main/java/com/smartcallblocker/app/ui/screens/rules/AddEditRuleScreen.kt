package com.smartcallblocker.app.ui.screens.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.R
import com.smartcallblocker.app.domain.model.RuleType
import com.smartcallblocker.app.ui.components.ScreenTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    ruleId: Long?,
    onBack: () -> Unit,
    vm: AddEditRuleViewModel = hiltViewModel(),
) {
    LaunchedEffect(ruleId) { vm.load(ruleId) }
    val state by vm.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = if (ruleId == null) "New Rule" else "Edit Rule",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::onName,
                label = { Text("Rule name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Rule type dropdown
            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
            ) {
                OutlinedTextField(
                    value = state.type.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rule type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                ) {
                    RuleType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = { vm.onType(type); typeExpanded = false },
                        )
                    }
                }
            }

            if (state.type.needsValue) {
                OutlinedTextField(
                    value = state.value,
                    onValueChange = vm::onValue,
                    label = { Text(valueLabel(state.type)) },
                    placeholder = { Text(valuePlaceholder(state.type)) },
                    singleLine = state.type != RuleType.REGEX,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text("Action when matched", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "REJECT" to "Reject",
                    "SILENCE" to "Silence",
                    "DISALLOW" to "Send to voicemail",
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = state.action == key,
                        onClick = { vm.onAction(key) },
                        label = { Text(label) },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", modifier = Modifier.weight(1f))
                Switch(checked = state.enabled, onCheckedChange = vm::onEnabled)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = vm::save,
                enabled = state.name.isNotBlank() &&
                    (!state.type.needsValue || state.value.isNotBlank()) && !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

private fun valueLabel(type: RuleType): String = when (type) {
    RuleType.STARTS_WITH -> "Prefix (e.g. +91, 1800)"
    RuleType.ENDS_WITH -> "Suffix (e.g. 0000)"
    RuleType.CONTAINS -> "Contains digits"
    RuleType.EXACT -> "Exact number"
    RuleType.LESS_THAN_DIGITS -> "Maximum digits"
    RuleType.GREATER_THAN_DIGITS -> "Minimum digits"
    RuleType.COUNTRY_CODE -> "Country code (e.g. +92)"
    RuleType.AREA_CODE -> "Area code"
    RuleType.REGEX -> "Regex pattern"
    RuleType.SPAM_SCORE -> "Score threshold"
    else -> "Value"
}

private fun valuePlaceholder(type: RuleType): String = when (type) {
    RuleType.STARTS_WITH -> "+91"
    RuleType.ENDS_WITH -> "0000"
    RuleType.CONTAINS -> "999"
    RuleType.EXACT -> "+919876543210"
    RuleType.LESS_THAN_DIGITS -> "8"
    RuleType.GREATER_THAN_DIGITS -> "15"
    RuleType.COUNTRY_CODE -> "+92"
    RuleType.AREA_CODE -> "011"
    RuleType.REGEX -> "^\\+1(800|888)"
    RuleType.SPAM_SCORE -> "80"
    else -> ""
}
