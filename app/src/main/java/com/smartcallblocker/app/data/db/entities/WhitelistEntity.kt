package com.smartcallblocker.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "whitelist_numbers",
    indices = [Index(value = ["normalizedNumber"], unique = true)],
)
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val name: String?,
    val source: String,       // MANUAL | CONTACTS | IMPORT
    val createdAt: Long,
)
