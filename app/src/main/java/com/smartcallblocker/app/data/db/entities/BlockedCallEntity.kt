package com.smartcallblocker.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocked_calls",
    indices = [
        Index("normalizedNumber"),
        Index("callDateTime"),
    ],
)
data class BlockedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val callerName: String?,
    val callDateTime: Long,
    val action: String,           // BLOCKED, REJECTED, SILENCED
    val matchedRuleId: Long?,
    val matchedRuleName: String?,
    val matchedRuleType: String?,
    val countryCode: String?,
    val isKnownContact: Boolean,
    val spamScore: Int,
    val temporaryBlockedUntil: Long?,
    val notes: String? = null,
)
