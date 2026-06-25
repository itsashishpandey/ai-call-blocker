package com.smartcallblocker.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartcallblocker.app.domain.model.BlockAction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "scb_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val PROTECTION_ENABLED = booleanPreferencesKey("protection_enabled")
        val DEFAULT_ACTION = stringPreferencesKey("default_action")
        val SKIP_NOTIFICATION = booleanPreferencesKey("skip_notification")
        val SKIP_CALL_LOG = booleanPreferencesKey("skip_call_log")
        val SAVE_LOGS = booleanPreferencesKey("save_logs")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
        val REPEATED_ENABLED = booleanPreferencesKey("repeated_enabled")
        val REPEATED_LIMIT = intPreferencesKey("repeated_limit")
        val REPEATED_WINDOW_MIN = intPreferencesKey("repeated_window_min")
        val REPEATED_BLOCK_MIN = intPreferencesKey("repeated_block_min")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SAFE_MODE_EXPIRY = longPreferencesKey("safe_mode_expiry")
        val BACKUP_FOLDER_URI = stringPreferencesKey("backup_folder_uri")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val LAST_BACKUP_STATUS = stringPreferencesKey("last_backup_status")
        val BLOCK_CARRIER_SPAM = booleanPreferencesKey("block_carrier_spam")
        val BLOCK_ALL_UNKNOWN = booleanPreferencesKey("block_all_unknown")
        val BLOCK_LANDLINE = booleanPreferencesKey("block_landline")
        val BLOCK_TOLL_FREE = booleanPreferencesKey("block_toll_free")
        val LOCKDOWN_EXPIRY = longPreferencesKey("lockdown_expiry")
    }

    val protectionEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.PROTECTION_ENABLED] ?: true }
    val defaultAction: Flow<BlockAction> = context.dataStore.data.map {
        runCatching { BlockAction.valueOf(it[Keys.DEFAULT_ACTION] ?: BlockAction.REJECT.name) }
            .getOrDefault(BlockAction.REJECT)
    }
    val skipNotification: Flow<Boolean> = context.dataStore.data.map { it[Keys.SKIP_NOTIFICATION] ?: false }
    val skipCallLog: Flow<Boolean> = context.dataStore.data.map { it[Keys.SKIP_CALL_LOG] ?: false }
    val saveLogs: Flow<Boolean> = context.dataStore.data.map { it[Keys.SAVE_LOGS] ?: true }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        runCatching { ThemeMode.valueOf(it[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }
    val privacyMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.PRIVACY_MODE] ?: false }
    val appLock: Flow<Boolean> = context.dataStore.data.map { it[Keys.APP_LOCK] ?: false }
    val autoDeleteDays: Flow<Int> = context.dataStore.data.map { it[Keys.AUTO_DELETE_DAYS] ?: 90 }
    val repeatedEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.REPEATED_ENABLED] ?: true }
    val repeatedLimit: Flow<Int> = context.dataStore.data.map { it[Keys.REPEATED_LIMIT] ?: 3 }
    val repeatedWindowMin: Flow<Int> = context.dataStore.data.map { it[Keys.REPEATED_WINDOW_MIN] ?: 5 }
    val repeatedBlockMin: Flow<Int> = context.dataStore.data.map { it[Keys.REPEATED_BLOCK_MIN] ?: 30 }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }
    val safeModeExpiry: Flow<Long> = context.dataStore.data.map { it[Keys.SAFE_MODE_EXPIRY] ?: 0L }
    val backupFolderUri: Flow<String?> = context.dataStore.data.map { it[Keys.BACKUP_FOLDER_URI] }
    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_BACKUP_ENABLED] ?: true }
    val lastBackupTime: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_BACKUP_TIME] ?: 0L }
    val lastBackupStatus: Flow<String> = context.dataStore.data.map { it[Keys.LAST_BACKUP_STATUS] ?: "" }
    val blockCarrierSpam: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOCK_CARRIER_SPAM] ?: false }
    val blockAllUnknown: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOCK_ALL_UNKNOWN] ?: false }
    val blockLandline: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOCK_LANDLINE] ?: false }
    val blockTollFree: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOCK_TOLL_FREE] ?: false }
    val lockdownExpiry: Flow<Long> = context.dataStore.data.map { it[Keys.LOCKDOWN_EXPIRY] ?: 0L }

    suspend fun setProtectionEnabled(value: Boolean) =
        context.dataStore.edit { it[Keys.PROTECTION_ENABLED] = value }.let { Unit }

    suspend fun setDefaultAction(value: BlockAction) =
        context.dataStore.edit { it[Keys.DEFAULT_ACTION] = value.name }.let { Unit }

    suspend fun setSkipNotification(value: Boolean) =
        context.dataStore.edit { it[Keys.SKIP_NOTIFICATION] = value }.let { Unit }

    suspend fun setSkipCallLog(value: Boolean) =
        context.dataStore.edit { it[Keys.SKIP_CALL_LOG] = value }.let { Unit }

    suspend fun setSaveLogs(value: Boolean) =
        context.dataStore.edit { it[Keys.SAVE_LOGS] = value }.let { Unit }

    suspend fun setThemeMode(value: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME_MODE] = value.name }.let { Unit }

    suspend fun setPrivacyMode(value: Boolean) =
        context.dataStore.edit { it[Keys.PRIVACY_MODE] = value }.let { Unit }

    suspend fun setAppLock(value: Boolean) =
        context.dataStore.edit { it[Keys.APP_LOCK] = value }.let { Unit }

    suspend fun setAutoDeleteDays(value: Int) =
        context.dataStore.edit { it[Keys.AUTO_DELETE_DAYS] = value }.let { Unit }

    suspend fun setRepeatedEnabled(value: Boolean) =
        context.dataStore.edit { it[Keys.REPEATED_ENABLED] = value }.let { Unit }

    suspend fun setRepeatedLimit(value: Int) =
        context.dataStore.edit { it[Keys.REPEATED_LIMIT] = value }.let { Unit }

    suspend fun setRepeatedWindowMin(value: Int) =
        context.dataStore.edit { it[Keys.REPEATED_WINDOW_MIN] = value }.let { Unit }

    suspend fun setRepeatedBlockMin(value: Int) =
        context.dataStore.edit { it[Keys.REPEATED_BLOCK_MIN] = value }.let { Unit }

    suspend fun setOnboardingCompleted(value: Boolean) =
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = value }.let { Unit }

    suspend fun setSafeModeExpiry(value: Long) =
        context.dataStore.edit { it[Keys.SAFE_MODE_EXPIRY] = value }.let { Unit }

    suspend fun startSafeMode(durationMinutes: Int) {
        val expiry = System.currentTimeMillis() + durationMinutes * 60_000L
        setSafeModeExpiry(expiry)
    }

    suspend fun cancelSafeMode() = setSafeModeExpiry(0L)

    suspend fun setBackupFolderUri(value: String?) =
        context.dataStore.edit {
            if (value == null) it.remove(Keys.BACKUP_FOLDER_URI) else it[Keys.BACKUP_FOLDER_URI] = value
        }.let { Unit }

    suspend fun setAutoBackupEnabled(value: Boolean) =
        context.dataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = value }.let { Unit }

    suspend fun setLastBackupTime(value: Long) =
        context.dataStore.edit { it[Keys.LAST_BACKUP_TIME] = value }.let { Unit }

    suspend fun setLastBackupStatus(value: String) =
        context.dataStore.edit { it[Keys.LAST_BACKUP_STATUS] = value }.let { Unit }

    suspend fun setBlockCarrierSpam(value: Boolean) =
        context.dataStore.edit { it[Keys.BLOCK_CARRIER_SPAM] = value }.let { Unit }

    suspend fun setBlockAllUnknown(value: Boolean) =
        context.dataStore.edit { it[Keys.BLOCK_ALL_UNKNOWN] = value }.let { Unit }

    suspend fun setBlockLandline(value: Boolean) =
        context.dataStore.edit { it[Keys.BLOCK_LANDLINE] = value }.let { Unit }

    suspend fun setBlockTollFree(value: Boolean) =
        context.dataStore.edit { it[Keys.BLOCK_TOLL_FREE] = value }.let { Unit }

    suspend fun setLockdownExpiry(value: Long) =
        context.dataStore.edit { it[Keys.LOCKDOWN_EXPIRY] = value }.let { Unit }

    suspend fun startLockdown(durationMinutes: Int) {
        val expiry = System.currentTimeMillis() + durationMinutes * 60_000L
        setLockdownExpiry(expiry)
        // Lockdown wins over safe mode — they are opposite intents.
        setSafeModeExpiry(0L)
    }

    suspend fun cancelLockdown() = setLockdownExpiry(0L)

    // ----- snapshot helpers for the rule engine -----

    suspend fun protectionEnabledSnapshot(): Boolean = protectionEnabled.first()
    suspend fun defaultActionSnapshot(): BlockAction = defaultAction.first()
    suspend fun skipNotificationSnapshot(): Boolean = skipNotification.first()
    suspend fun skipCallLogSnapshot(): Boolean = skipCallLog.first()
    suspend fun saveLogsSnapshot(): Boolean = saveLogs.first()
    suspend fun repeatedCallsEnabledSnapshot(): Boolean = repeatedEnabled.first()
    suspend fun repeatedCallLimitSnapshot(): Int = repeatedLimit.first()
    suspend fun repeatedCallWindowMinutesSnapshot(): Int = repeatedWindowMin.first()
    suspend fun repeatedCallBlockMinutesSnapshot(): Int = repeatedBlockMin.first()
    suspend fun autoDeleteDaysSnapshot(): Int = autoDeleteDays.first()
    suspend fun privacyModeSnapshot(): Boolean = privacyMode.first()
    suspend fun onboardingCompletedSnapshot(): Boolean = onboardingCompleted.first()
    suspend fun safeModeExpirySnapshot(): Long = safeModeExpiry.first()
    suspend fun safeModeActive(): Boolean = safeModeExpirySnapshot() > System.currentTimeMillis()
    suspend fun backupFolderUriSnapshot(): String? = backupFolderUri.first()
    suspend fun autoBackupEnabledSnapshot(): Boolean = autoBackupEnabled.first()
    suspend fun lastBackupTimeSnapshot(): Long = lastBackupTime.first()
    suspend fun blockCarrierSpamSnapshot(): Boolean = blockCarrierSpam.first()
    suspend fun blockAllUnknownSnapshot(): Boolean = blockAllUnknown.first()
    suspend fun blockLandlineSnapshot(): Boolean = blockLandline.first()
    suspend fun blockTollFreeSnapshot(): Boolean = blockTollFree.first()
    suspend fun lockdownExpirySnapshot(): Long = lockdownExpiry.first()
    suspend fun lockdownActive(): Boolean = lockdownExpirySnapshot() > System.currentTimeMillis()

    /** Exports current settings as a flat map for backup. */
    suspend fun exportForBackup(): Map<String, Any?> = mapOf(
        "protectionEnabled" to protectionEnabled.first(),
        "defaultAction" to defaultAction.first().name,
        "skipNotification" to skipNotification.first(),
        "skipCallLog" to skipCallLog.first(),
        "saveLogs" to saveLogs.first(),
        "themeMode" to themeMode.first().name,
        "privacyMode" to privacyMode.first(),
        "appLock" to appLock.first(),
        "autoDeleteDays" to autoDeleteDays.first(),
        "repeatedEnabled" to repeatedEnabled.first(),
        "repeatedLimit" to repeatedLimit.first(),
        "repeatedWindowMin" to repeatedWindowMin.first(),
        "repeatedBlockMin" to repeatedBlockMin.first(),
        "autoBackupEnabled" to autoBackupEnabled.first(),
        "blockCarrierSpam" to blockCarrierSpam.first(),
        "blockAllUnknown" to blockAllUnknown.first(),
        "blockLandline" to blockLandline.first(),
        "blockTollFree" to blockTollFree.first(),
    )

    /** Applies a settings map from backup (only known keys). */
    suspend fun applyFromBackup(map: Map<String, Any?>) {
        (map["protectionEnabled"] as? Boolean)?.let { setProtectionEnabled(it) }
        (map["defaultAction"] as? String)?.let {
            runCatching { com.smartcallblocker.app.domain.model.BlockAction.valueOf(it) }
                .getOrNull()?.let { action -> setDefaultAction(action) }
        }
        (map["skipNotification"] as? Boolean)?.let { setSkipNotification(it) }
        (map["skipCallLog"] as? Boolean)?.let { setSkipCallLog(it) }
        (map["saveLogs"] as? Boolean)?.let { setSaveLogs(it) }
        (map["themeMode"] as? String)?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()?.let { mode -> setThemeMode(mode) }
        }
        (map["privacyMode"] as? Boolean)?.let { setPrivacyMode(it) }
        (map["appLock"] as? Boolean)?.let { setAppLock(it) }
        (map["autoDeleteDays"] as? Number)?.let { setAutoDeleteDays(it.toInt()) }
        (map["repeatedEnabled"] as? Boolean)?.let { setRepeatedEnabled(it) }
        (map["repeatedLimit"] as? Number)?.let { setRepeatedLimit(it.toInt()) }
        (map["repeatedWindowMin"] as? Number)?.let { setRepeatedWindowMin(it.toInt()) }
        (map["repeatedBlockMin"] as? Number)?.let { setRepeatedBlockMin(it.toInt()) }
        (map["autoBackupEnabled"] as? Boolean)?.let { setAutoBackupEnabled(it) }
        (map["blockCarrierSpam"] as? Boolean)?.let { setBlockCarrierSpam(it) }
        (map["blockAllUnknown"] as? Boolean)?.let { setBlockAllUnknown(it) }
        (map["blockLandline"] as? Boolean)?.let { setBlockLandline(it) }
        (map["blockTollFree"] as? Boolean)?.let { setBlockTollFree(it) }
    }
}
