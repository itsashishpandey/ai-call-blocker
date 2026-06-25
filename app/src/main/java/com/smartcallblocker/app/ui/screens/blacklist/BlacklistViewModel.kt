package com.smartcallblocker.app.ui.screens.blacklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.BlacklistEntity
import com.smartcallblocker.app.data.repository.BlacklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    private val repo: BlacklistRepository,
) : ViewModel() {

    val items: StateFlow<List<BlacklistEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(phone: String, reason: String?) = viewModelScope.launch {
        if (phone.isBlank()) return@launch
        repo.add(phone.trim(), reason?.trim()?.takeIf { it.isNotBlank() })
    }

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
}
