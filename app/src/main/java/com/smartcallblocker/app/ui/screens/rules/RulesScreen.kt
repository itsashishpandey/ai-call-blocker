package com.smartcallblocker.app.ui.screens.rules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.R
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import com.smartcallblocker.app.domain.model.RuleType
import com.smartcallblocker.app.ui.components.EmptyState
import com.smartcallblocker.app.ui.components.ScreenTopBar

@Composable
fun RulesScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    vm: RulesViewModel = hiltViewModel(),
) {
    val rules by vm.rules.collectAsState()

    Scaffold(
        topBar = { ScreenTopBar(title = stringResource(R.string.nav_rules), onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_rule)) },
            )
        },
    ) { padding ->
        if (rules.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Tune,
                title = stringResource(R.string.empty_rules),
                description = stringResource(R.string.empty_rules_desc),
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
                items(rules, key = { it.id }) { rule ->
                    RuleItem(
                        rule = rule,
                        onToggle = { vm.toggle(rule, it) },
                        onClick = { onEdit(rule.id) },
                        onDelete = { vm.delete(rule.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    rule: BlockingRuleEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.ruleName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                val type = RuleType.fromName(rule.ruleType)
                val typeLabel = type?.displayName ?: rule.ruleType
                Text(
                    text = if (rule.ruleValue.isNotBlank() && type?.needsValue == true)
                        "$typeLabel: ${rule.ruleValue}" else typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Action: ${rule.action.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(checked = rule.isEnabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
