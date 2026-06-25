package com.smartcallblocker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedCallDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(call: BlockedCallEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calls: List<BlockedCallEntity>)

    @Query("SELECT * FROM blocked_calls ORDER BY callDateTime DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<BlockedCallEntity>>

    @Query("SELECT * FROM blocked_calls ORDER BY callDateTime DESC")
    fun observeAll(): Flow<List<BlockedCallEntity>>

    @Query("SELECT * FROM blocked_calls WHERE normalizedNumber = :number ORDER BY callDateTime DESC")
    fun observeByNumber(number: String): Flow<List<BlockedCallEntity>>

    @Query("SELECT COUNT(*) FROM blocked_calls WHERE callDateTime >= :since")
    fun countSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_calls")
    fun countAll(): Flow<Int>

    @Query("SELECT * FROM blocked_calls ORDER BY callDateTime DESC LIMIT 1")
    fun observeLatest(): Flow<BlockedCallEntity?>

    @Query("DELETE FROM blocked_calls WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM blocked_calls")
    suspend fun deleteAll()

    @Query("DELETE FROM blocked_calls WHERE callDateTime < :before")
    suspend fun deleteOlderThan(before: Long)
}
