package com.smartcallblocker.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.data.preferences.ThemeMode
import com.smartcallblocker.app.domain.model.BlockAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val protectionEnabled: Boolean = true,
    val action: BlockAction = BlockAction.REJECT,
    val skipNotification: Boolean = false,
    val skipCallLog: Boolean = false,
    val saveLogs: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val privacyMode: Boolean = false,
    val appLock: Boolean = false,
    val autoDeleteDays: Int = 90,
    val repeatedEnabled: Boolean = true,
    val repeatedLimit: Int = 3,
    val repeatedWindowMin: Int = 5,
    val repeatedBlockMin: Int = 30,
    val blockCarrierSpam: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observe(settings.protectionEnabled) { _state.value = _state.value.copy(protectionEnabled = it) }
        observe(settings.defaultAction) { _state.value = _state.value.copy(action = it) }
        observe(settings.skipNotification) { _state.value = _state.value.copy(skipNotification = it) }
        observe(settings.skipCallLog) { _state.value = _state.value.copy(skipCallLog = it) }
        observe(settings.saveLogs) { _state.value = _state.value.copy(saveLogs = it) }
        observe(settings.themeMode) { _state.value = _state.value.copy(themeMode = it) }
        observe(settings.privacyMode) { _state.value = _state.value.copy(privacyMode = it) }
        observe(settings.appLock) { _state.value = _state.value.copy(appLock = it) }
        observe(settings.autoDeleteDays) { _state.value = _state.value.copy(autoDeleteDays = it) }
        observe(settings.repeatedEnabled) { _state.value = _state.value.copy(repeatedEnabled = it) }
        observe(settings.repeatedLimit) { _state.value = _state.value.copy(repeatedLimit = it) }
        observe(settings.repeatedWindowMin) { _state.value = _state.value.copy(repeatedWindowMin = it) }
        observe(settings.repeatedBlockMin) { _state.value = _state.value.copy(repeatedBlockMin = it) }
        observe(settings.blockCarrierSpam) { _state.value = _state.value.copy(blockCarrierSpam = it) }
    }

    private fun <T> observe(flow: kotlinx.coroutines.flow.Flow<T>, block: (T) -> Unit) {
        viewModelScope.launch { flow.collectLatest(block) }
    }

    fun setProtection(v: Boolean) = viewModelScope.launch { settings.setProtectionEnabled(v) }
    fun setAction(v: BlockAction) = viewModelScope.launch { settings.setDefaultAction(v) }
    fun setSkipNotification(v: Boolean) = viewModelScope.launch { settings.setSkipNotification(v) }
    fun setSkipCallLog(v: Boolean) = viewModelScope.launch { settings.setSkipCallLog(v) }
    fun setSaveLogs(v: Boolean) = viewModelScope.launch { settings.setSaveLogs(v) }
    fun setThemeMode(v: ThemeMode) = viewModelScope.launch { settings.setThemeMode(v) }
    fun setPrivacyMode(v: Boolean) = viewModelScope.launch { settings.setPrivacyMode(v) }
    fun setAppLock(v: Boolean) = viewModelScope.launch { settings.setAppLock(v) }
    fun setAutoDeleteDays(v: Int) = viewModelScope.launch { settings.setAutoDeleteDays(v) }
    fun setRepeatedEnabled(v: Boolean) = viewModelScope.launch { settings.setRepeatedEnabled(v) }
    fun setRepeatedLimit(v: Int) = viewModelScope.launch { settings.setRepeatedLimit(v) }
    fun setRepeatedWindow(v: Int) = viewModelScope.launch { settings.setRepeatedWindowMin(v) }
    fun setRepeatedBlock(v: Int) = viewModelScope.launch { settings.setRepeatedBlockMin(v) }
    fun setBlockCarrierSpam(v: Boolean) = viewModelScope.launch { settings.setBlockCarrierSpam(v) }
}
