package com.smartcallblocker.app.data.backup

import com.smartcallblocker.app.data.db.entities.BlacklistEntity
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import com.smartcallblocker.app.data.db.entities.TemporaryBlockEntity
import com.smartcallblocker.app.data.db.entities.WhitelistEntity

/**
 * Full snapshot of user data + settings. Serialised to JSON and written to a
 * user-picked folder via SAF. Survives uninstall as long as the folder does.
 */
data class BackupBundle(
    val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    val appVersion: String = "",
    val publisher: String = "Triple Minds",
    val timestamp: Long = 0L,
    val settings: Map<String, Any?> = emptyMap(),
    val rules: List<BlockingRuleEntity> = emptyList(),
    val whitelist: List<WhitelistEntity> = emptyList(),
    val blacklist: List<BlacklistEntity> = emptyList(),
    val blockedCalls: List<BlockedCallEntity> = emptyList(),
    val temporaryBlocks: List<TemporaryBlockEntity> = emptyList(),
)

const val BACKUP_SCHEMA_VERSION = 1
const val BACKUP_FILENAME = "smart_call_blocker_backup.json"
const val BACKUP_MIME_TYPE = "application/json"
