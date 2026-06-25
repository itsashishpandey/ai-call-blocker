package com.smartcallblocker.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val completed: StateFlow<Boolean> = settings.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _finishing = MutableStateFlow(false)
    val finishing: StateFlow<Boolean> = _finishing.asStateFlow()

    fun finish() {
        _finishing.value = true
        viewModelScope.launch {
            settings.setOnboardingCompleted(true)
        }
    }
}
