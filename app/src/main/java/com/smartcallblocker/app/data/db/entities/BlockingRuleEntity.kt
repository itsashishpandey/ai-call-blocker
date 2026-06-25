package com.smartcallblocker.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_rules")
data class BlockingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val ruleType: String,         // e.g. STARTS_WITH, CONTAINS, REGEX, UNKNOWN, PRIVATE, EMPTY...
    val ruleValue: String,        // pattern, prefix, etc. (empty for switch-style rules)
    val isEnabled: Boolean = true,
    val priority: Int = 50,       // lower = higher priority
    val action: String = "REJECT", // REJECT | SILENCE | BLOCK
    val scheduleStart: Int? = null, // minute-of-day 0..1439
    val scheduleEnd: Int? = null,
    val scheduleDaysMask: Int? = null, // bitmask Sun=1..Sat=64
    val createdAt: Long,
    val updatedAt: Long,
)
