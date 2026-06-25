package com.smartcallblocker.app.ui.screens.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.data.repository.BlockedCallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedCallsViewModel @Inject constructor(
    private val repo: BlockedCallRepository,
    settings: SettingsRepository,
) : ViewModel() {

    val calls: StateFlow<List<BlockedCallEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val privacyMode: StateFlow<Boolean> = settings.privacyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
    fun clearAll() = viewModelScope.launch { repo.deleteAll() }
}
