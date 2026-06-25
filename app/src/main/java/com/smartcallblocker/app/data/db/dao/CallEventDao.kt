package com.smartcallblocker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smartcallblocker.app.data.db.entities.CallEventEntity

@Dao
interface CallEventDao {

    @Insert
    suspend fun insert(event: CallEventEntity): Long

    @Query("SELECT COUNT(*) FROM call_events WHERE normalizedNumber = :number AND eventTime >= :sinceTime")
    suspend fun countSince(number: String, sinceTime: Long): Int

    @Query("DELETE FROM call_events WHERE eventTime < :before")
    suspend fun purgeOlderThan(before: Long)

    @Query("DELETE FROM call_events")
    suspend fun deleteAll()
}
