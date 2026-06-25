package com.smartcallblocker.app.ui.screens.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import com.smartcallblocker.app.data.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repo: RuleRepository,
) : ViewModel() {

    val rules: StateFlow<List<BlockingRuleEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(rule: BlockingRuleEntity, enabled: Boolean) = viewModelScope.launch {
        repo.toggle(rule, enabled)
    }

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
}
