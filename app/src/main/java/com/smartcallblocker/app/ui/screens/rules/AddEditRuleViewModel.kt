package com.smartcallblocker.app.ui.screens.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import com.smartcallblocker.app.data.repository.RuleRepository
import com.smartcallblocker.app.domain.model.RuleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditRuleUiState(
    val id: Long = 0,
    val name: String = "",
    val type: RuleType = RuleType.STARTS_WITH,
    val value: String = "",
    val action: String = "REJECT",
    val enabled: Boolean = true,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class AddEditRuleViewModel @Inject constructor(
    private val repo: RuleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditRuleUiState())
    val state: StateFlow<AddEditRuleUiState> = _state.asStateFlow()

    fun load(id: Long?) {
        if (id == null || id == 0L) return
        viewModelScope.launch {
            val rule = repo.get(id) ?: return@launch
            _state.value = _state.value.copy(
                id = rule.id,
                name = rule.ruleName,
                type = RuleType.fromName(rule.ruleType) ?: RuleType.STARTS_WITH,
                value = rule.ruleValue,
                action = rule.action,
                enabled = rule.isEnabled,
            )
        }
    }

    fun onName(v: String) { _state.value = _state.value.copy(name = v) }
    fun onType(t: RuleType) { _state.value = _state.value.copy(type = t) }
    fun onValue(v: String) { _state.value = _state.value.copy(value = v) }
    fun onAction(a: String) { _state.value = _state.value.copy(action = a) }
    fun onEnabled(v: Boolean) { _state.value = _state.value.copy(enabled = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        if (s.type.needsValue && s.value.isBlank()) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val rule = BlockingRuleEntity(
                id = s.id,
                ruleName = s.name.trim(),
                ruleType = s.type.name,
                ruleValue = s.value.trim(),
                isEnabled = s.enabled,
                priority = 50,
                action = s.action,
                createdAt = if (s.id == 0L) now else now,
                updatedAt = now,
            )
            repo.upsert(rule)
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }
}
