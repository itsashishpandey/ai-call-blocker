package com.smartcallblocker.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.data.repository.BlockedCallRepository
import com.smartcallblocker.app.data.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val protectionEnabled: Boolean = true,
    val privacyMode: Boolean = false,
    val blockedToday: Int = 0,
    val blockedWeek: Int = 0,
    val blockedMonth: Int = 0,
    val blockedTotal: Int = 0,
    val activeRules: Int = 0,
    val lastBlocked: BlockedCallEntity? = null,
    val safeModeExpiry: Long = 0L,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val blockedRepo: BlockedCallRepository,
    private val ruleRepo: RuleRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        observe(settings.protectionEnabled) { value -> _state.value = _state.value.copy(protectionEnabled = value) }
        observe(settings.privacyMode) { value -> _state.value = _state.value.copy(privacyMode = value) }
        observe(blockedRepo.countSince(startOfToday())) { value -> _state.value = _state.value.copy(blockedToday = value) }
        observe(blockedRepo.countSince(startOfWeek())) { value -> _state.value = _state.value.copy(blockedWeek = value) }
        observe(blockedRepo.countSince(startOfMonth())) { value -> _state.value = _state.value.copy(blockedMonth = value) }
        observe(blockedRepo.countAll()) { value -> _state.value = _state.value.copy(blockedTotal = value) }
        observe(ruleRepo.countEnabled()) { value -> _state.value = _state.value.copy(activeRules = value) }
        observe(blockedRepo.observeLatest()) { value -> _state.value = _state.value.copy(lastBlocked = value) }
        observe(settings.safeModeExpiry) { value -> _state.value = _state.value.copy(safeModeExpiry = value) }
    }

    private fun <T> observe(flow: kotlinx.coroutines.flow.Flow<T>, block: (T) -> Unit) {
        viewModelScope.launch {
            flow.collectLatest(block)
        }
    }

    fun toggleProtection(enabled: Boolean) = viewModelScope.launch {
        settings.setProtectionEnabled(enabled)
    }

    fun startSafeMode(durationMinutes: Int) = viewModelScope.launch {
        settings.startSafeMode(durationMinutes)
    }

    fun cancelSafeMode() = viewModelScope.launch { settings.cancelSafeMode() }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfWeek(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }.timeInMillis

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis
}
