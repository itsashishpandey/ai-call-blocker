package com.smartcallblocker.app.ui.screens.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.WhitelistEntity
import com.smartcallblocker.app.data.repository.WhitelistRepository
import com.smartcallblocker.app.util.ContactsLookup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhitelistViewModel @Inject constructor(
    private val repo: WhitelistRepository,
    private val contacts: ContactsLookup,
) : ViewModel() {

    val items: StateFlow<List<WhitelistEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(phone: String, name: String?) = viewModelScope.launch {
        if (phone.isBlank()) return@launch
        repo.add(phone.trim(), name?.trim()?.takeIf { it.isNotBlank() })
    }

    fun importContacts() = viewModelScope.launch {
        val pairs = contacts.allContactNumbers().map { it.second to it.first }
        repo.addAll(pairs)
    }

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
}
