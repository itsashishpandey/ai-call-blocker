package com.smartcallblocker.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "temporary_blocked_numbers",
    indices = [Index(value = ["normalizedNumber"], unique = true)],
)
data class TemporaryBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val callCount: Int,
    val firstCallTime: Long,
    val lastCallTime: Long,
    val blockedUntil: Long,
    val ruleId: Long?,
    val isActive: Boolean,
)
