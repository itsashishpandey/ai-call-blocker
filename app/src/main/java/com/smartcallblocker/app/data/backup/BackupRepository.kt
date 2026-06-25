package com.smartcallblocker.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smartcallblocker.app.BuildConfig
import com.smartcallblocker.app.data.db.AppDatabase
import com.smartcallblocker.app.data.db.dao.BlacklistDao
import com.smartcallblocker.app.data.db.dao.BlockedCallDao
import com.smartcallblocker.app.data.db.dao.BlockingRuleDao
import com.smartcallblocker.app.data.db.dao.TemporaryBlockDao
import com.smartcallblocker.app.data.db.dao.WhitelistDao
import com.smartcallblocker.app.data.preferences.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auto-persists user data to a SAF folder the user picked. Any insert/update/delete on
 * rules, whitelist, blacklist, blocked-calls or settings triggers a debounced write
 * to backup.json in that folder. On a fresh install, calling [restoreFromFolder] reads
 * the same file and replays every entity + setting.
 */
@OptIn(FlowPreview::class)
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val ruleDao: BlockingRuleDao,
    private val whitelistDao: WhitelistDao,
    private val blacklistDao: BlacklistDao,
    private val blockedCallDao: BlockedCallDao,
    private val tempBlockDao: TemporaryBlockDao,
    private val settings: SettingsRepository,
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeMutex = Mutex()
    private val trigger = Channel<Unit>(Channel.CONFLATED)

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    /** Start watching for changes. Called once from Application#onCreate. */
    fun start() {
        scope.launch {
            trigger.consumeAsFlow()
                .debounce(DEBOUNCE_MS)
                .collect {
                    if (settings.autoBackupEnabledSnapshot() &&
                        settings.backupFolderUriSnapshot() != null
                    ) {
                        runCatching { writeBackup() }
                            .onFailure { Log.e(TAG, "Auto-backup failed", it) }
                    }
                }
        }

        // Wire observers — skip first emission (initial cold load) so we don't backup at startup
        // when nothing has actually changed.
        scope.launch { ruleDao.observeAll().drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { whitelistDao.observeAll().drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { blacklistDao.observeAll().drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { blockedCallDao.observeAll().drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { tempBlockDao.observeAll().drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        // Settings observers
        scope.launch { settings.protectionEnabled.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.defaultAction.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.privacyMode.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.repeatedEnabled.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.repeatedLimit.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.repeatedWindowMin.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.repeatedBlockMin.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.saveLogs.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.themeMode.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.appLock.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
        scope.launch { settings.autoDeleteDays.drop(1).distinctUntilChanged().collect { trigger.trySend(Unit) } }
    }

    /** Take persistent permission for the folder the user just picked and immediately back up. */
    suspend fun configureFolder(folderUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(folderUri, flags)
            settings.setBackupFolderUri(folderUri.toString())
            settings.setAutoBackupEnabled(true)
            writeBackup()
        } catch (t: Throwable) {
            Log.e(TAG, "configureFolder failed", t)
            BackupResult.Failure(t.message ?: "Unknown error")
        }
    }

    /** Release the folder and disable auto-backup. */
    suspend fun forgetFolder(): Unit = withContext(Dispatchers.IO) {
        val uriString = settings.backupFolderUriSnapshot() ?: return@withContext
        runCatching {
            val uri = Uri.parse(uriString)
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.releasePersistableUriPermission(uri, flags)
        }
        settings.setBackupFolderUri(null)
        settings.setLastBackupTime(0L)
        settings.setLastBackupStatus("Backup folder disconnected")
    }

    /** Force an immediate backup. */
    suspend fun backupNow(): BackupResult = withContext(Dispatchers.IO) {
        if (settings.backupFolderUriSnapshot() == null) {
            return@withContext BackupResult.Failure("No backup folder configured")
        }
        runCatching { writeBackup() }.getOrElse { BackupResult.Failure(it.message ?: "Error") }
    }

    /**
     * If a backup file exists in the currently configured folder, return its metadata
     * (for the "Restore previous data?" prompt). Returns null if no folder or no file.
     */
    suspend fun probeExistingBackup(): BackupProbe? = withContext(Dispatchers.IO) {
        val uriString = settings.backupFolderUriSnapshot() ?: return@withContext null
        val parentUri = Uri.parse(uriString)
        val parent = DocumentFile.fromTreeUri(context, parentUri) ?: return@withContext null
        val file = parent.findFile(BACKUP_FILENAME) ?: return@withContext null
        if (!file.canRead()) return@withContext null
        BackupProbe(
            uri = file.uri,
            lastModified = file.lastModified(),
            sizeBytes = file.length(),
        )
    }

    /**
     * Probe an arbitrary folder URI (used right after the user picks one) so we can show
     * the restore prompt before we take persistable permission.
     */
    suspend fun probeFolder(folderUri: Uri): BackupProbe? = withContext(Dispatchers.IO) {
        runCatching {
            val parent = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext null
            val file = parent.findFile(BACKUP_FILENAME) ?: return@withContext null
            if (!file.canRead()) return@withContext null
            BackupProbe(file.uri, file.lastModified(), file.length())
        }.getOrNull()
    }

    /** Read backup.json from the configured folder and replace local data with it. */
    suspend fun restoreFromConfiguredFolder(): BackupResult = withContext(Dispatchers.IO) {
        val probe = probeExistingBackup() ?: return@withContext BackupResult.Failure("No backup file found")
        runCatching {
            val json = context.contentResolver.openInputStream(probe.uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: throw IllegalStateException("Cannot open backup file")
            val bundle = gson.fromJson(json, BackupBundle::class.java)
            if (bundle.schemaVersion > BACKUP_SCHEMA_VERSION) {
                throw IllegalStateException(
                    "Backup was created by a newer version of the app (schema ${bundle.schemaVersion}). Update the app to restore."
                )
            }
            applyBundle(bundle)
            BackupResult.Success(
                timestamp = bundle.timestamp,
                rulesCount = bundle.rules.size,
                whitelistCount = bundle.whitelist.size,
                blacklistCount = bundle.blacklist.size,
                blockedCount = bundle.blockedCalls.size,
            )
        }.getOrElse {
            Log.e(TAG, "Restore failed", it)
            BackupResult.Failure(it.message ?: "Restore failed")
        }
    }

    // -------------------------------------------------------------------- internal

    private suspend fun writeBackup(): BackupResult = writeMutex.withLock {
        val uriString = settings.backupFolderUriSnapshot()
            ?: return BackupResult.Failure("No backup folder configured")
        val parentUri = Uri.parse(uriString)
        val parent = DocumentFile.fromTreeUri(context, parentUri)
            ?: return BackupResult.Failure("Backup folder is unreachable. Re-pick the folder in Settings.")

        val bundle = collectBundle()
        val json = gson.toJson(bundle)

        // Strategy: write to a temp file then rename, to avoid corruption mid-write.
        val tmpName = "$BACKUP_FILENAME.tmp"
        parent.findFile(tmpName)?.delete()
        val tmp = parent.createFile(BACKUP_MIME_TYPE, tmpName)
            ?: return BackupResult.Failure("Cannot create temp file in folder")
        context.contentResolver.openOutputStream(tmp.uri, "wt")?.use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
            out.flush()
        } ?: run {
            tmp.delete()
            return BackupResult.Failure("Cannot open output stream")
        }

        // Atomically replace the main file
        parent.findFile(BACKUP_FILENAME)?.delete()
        tmp.renameTo(BACKUP_FILENAME)

        val ts = System.currentTimeMillis()
        settings.setLastBackupTime(ts)
        settings.setLastBackupStatus("Saved")
        _state.value = _state.value.copy(lastSuccess = ts, lastError = null)

        BackupResult.Success(
            timestamp = ts,
            rulesCount = bundle.rules.size,
            whitelistCount = bundle.whitelist.size,
            blacklistCount = bundle.blacklist.size,
            blockedCount = bundle.blockedCalls.size,
        )
    }

    private suspend fun collectBundle(): BackupBundle = BackupBundle(
        schemaVersion = BACKUP_SCHEMA_VERSION,
        appVersion = BuildConfig.VERSION_NAME,
        publisher = "Triple Minds",
        timestamp = System.currentTimeMillis(),
        settings = settings.exportForBackup(),
        rules = ruleDao.observeAll().first(),
        whitelist = whitelistDao.observeAll().first(),
        blacklist = blacklistDao.observeAll().first(),
        blockedCalls = blockedCallDao.observeAll().first(),
        temporaryBlocks = tempBlockDao.observeAll().first(),
    )

    private suspend fun applyBundle(bundle: BackupBundle) {
        // Clear and refill atomically so we never end up with a half-merged state.
        db.withTransaction {
            ruleDao.deleteAll()
            whitelistDao.deleteAll()
            blacklistDao.deleteAll()
            blockedCallDao.deleteAll()
            tempBlockDao.deleteAll()
            if (bundle.rules.isNotEmpty()) ruleDao.insertAll(bundle.rules)
            if (bundle.whitelist.isNotEmpty()) whitelistDao.insertAll(bundle.whitelist)
            if (bundle.blacklist.isNotEmpty()) blacklistDao.insertAll(bundle.blacklist)
            if (bundle.blockedCalls.isNotEmpty()) blockedCallDao.insertAll(bundle.blockedCalls)
            if (bundle.temporaryBlocks.isNotEmpty()) tempBlockDao.insertAll(bundle.temporaryBlocks)
        }
        settings.applyFromBackup(bundle.settings)
    }

    data class BackupState(
        val lastSuccess: Long = 0L,
        val lastError: String? = null,
    )

    companion object {
        private const val TAG = "BackupRepo"
        private const val DEBOUNCE_MS = 1200L
    }
}

data class BackupProbe(
    val uri: Uri,
    val lastModified: Long,
    val sizeBytes: Long,
)

sealed interface BackupResult {
    data class Success(
        val timestamp: Long,
        val rulesCount: Int,
        val whitelistCount: Int,
        val blacklistCount: Int,
        val blockedCount: Int,
    ) : BackupResult

    data class Failure(val message: String) : BackupResult
}
