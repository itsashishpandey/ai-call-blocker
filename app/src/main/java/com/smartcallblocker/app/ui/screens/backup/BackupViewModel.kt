package com.smartcallblocker.app.ui.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.backup.BackupProbe
import com.smartcallblocker.app.data.backup.BackupRepository
import com.smartcallblocker.app.data.backup.BackupResult
import com.smartcallblocker.app.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val folderUri: String? = null,
    val autoBackupEnabled: Boolean = true,
    val lastBackupTime: Long = 0L,
    val lastStatus: String = "",
    val pendingFolderUri: Uri? = null,
    val pendingProbe: BackupProbe? = null,
    val showRestorePrompt: Boolean = false,
    val isWorking: Boolean = false,
    val snackbar: String? = null,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backup: BackupRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { settings.backupFolderUri.collectLatest { v -> _state.value = _state.value.copy(folderUri = v) } }
        viewModelScope.launch { settings.autoBackupEnabled.collectLatest { v -> _state.value = _state.value.copy(autoBackupEnabled = v) } }
        viewModelScope.launch { settings.lastBackupTime.collectLatest { v -> _state.value = _state.value.copy(lastBackupTime = v) } }
        viewModelScope.launch { settings.lastBackupStatus.collectLatest { v -> _state.value = _state.value.copy(lastStatus = v) } }
    }

    /** Called after the user picks a folder via SAF. Probes for existing backup so we can offer restore. */
    fun onFolderPicked(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isWorking = true)
            val probe = backup.probeFolder(uri)
            if (probe != null) {
                _state.value = _state.value.copy(
                    pendingFolderUri = uri,
                    pendingProbe = probe,
                    showRestorePrompt = true,
                    isWorking = false,
                )
            } else {
                // No existing backup → just configure this folder + write first backup.
                val result = backup.configureFolder(uri)
                _state.value = _state.value.copy(
                    isWorking = false,
                    pendingFolderUri = null,
                    pendingProbe = null,
                    snackbar = when (result) {
                        is BackupResult.Success -> "Backup folder set up. Saved ${result.totalItems()} items."
                        is BackupResult.Failure -> "Could not set up folder: ${result.message}"
                    },
                )
            }
        }
    }

    /** User confirmed they want to restore the existing backup file. */
    fun confirmRestore() {
        val uri = _state.value.pendingFolderUri ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isWorking = true, showRestorePrompt = false)
            // Configure the folder first so the URI permission sticks.
            backup.configureFolder(uri)
            val result = backup.restoreFromConfiguredFolder()
            _state.value = _state.value.copy(
                isWorking = false,
                pendingFolderUri = null,
                pendingProbe = null,
                snackbar = when (result) {
                    is BackupResult.Success -> "Restored ${result.totalItems()} items from backup."
                    is BackupResult.Failure -> "Restore failed: ${result.message}"
                },
            )
        }
    }

    /** User declined restore — keep the folder, overwrite the file with current (empty/new) data. */
    fun declineRestore() {
        val uri = _state.value.pendingFolderUri ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isWorking = true, showRestorePrompt = false)
            val result = backup.configureFolder(uri)
            _state.value = _state.value.copy(
                isWorking = false,
                pendingFolderUri = null,
                pendingProbe = null,
                snackbar = when (result) {
                    is BackupResult.Success -> "Folder set up. Local data is now being saved here."
                    is BackupResult.Failure -> "Could not set up folder: ${result.message}"
                },
            )
        }
    }

    fun dismissRestorePrompt() {
        _state.value = _state.value.copy(
            showRestorePrompt = false,
            pendingFolderUri = null,
            pendingProbe = null,
        )
    }

    fun backupNow() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isWorking = true)
            val r = backup.backupNow()
            _state.value = _state.value.copy(
                isWorking = false,
                snackbar = when (r) {
                    is BackupResult.Success -> "Saved ${r.totalItems()} items."
                    is BackupResult.Failure -> "Backup failed: ${r.message}"
                },
            )
        }
    }

    fun restoreNow() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isWorking = true)
            val r = backup.restoreFromConfiguredFolder()
            _state.value = _state.value.copy(
                isWorking = false,
                snackbar = when (r) {
                    is BackupResult.Success -> "Restored ${r.totalItems()} items."
                    is BackupResult.Failure -> "Restore failed: ${r.message}"
                },
            )
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            backup.forgetFolder()
            _state.value = _state.value.copy(snackbar = "Backup folder disconnected.")
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch { settings.setAutoBackupEnabled(enabled) }
    }

    fun clearSnackbar() {
        _state.value = _state.value.copy(snackbar = null)
    }
}

private fun BackupResult.Success.totalItems(): Int =
    rulesCount + whitelistCount + blacklistCount + blockedCount
