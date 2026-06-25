package com.smartcallblocker.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blacklist_numbers",
    indices = [Index(value = ["normalizedNumber"], unique = true)],
)
data class BlacklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val reason: String?,
    val createdAt: Long,
)
