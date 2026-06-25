package com.smartcallblocker.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Lightweight ring of recent incoming events used for repeated-call detection. */
@Entity(
    tableName = "call_events",
    indices = [
        Index("normalizedNumber"),
        Index("eventTime"),
    ],
)
data class CallEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedNumber: String,
    val eventTime: Long,
)
