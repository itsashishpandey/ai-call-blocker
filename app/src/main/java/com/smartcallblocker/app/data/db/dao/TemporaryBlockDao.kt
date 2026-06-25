package com.smartcallblocker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartcallblocker.app.data.db.entities.TemporaryBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemporaryBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TemporaryBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TemporaryBlockEntity>)

    @Query("DELETE FROM temporary_blocked_numbers")
    suspend fun deleteAll()

    @Update
    suspend fun update(entity: TemporaryBlockEntity)

    @Query("SELECT * FROM temporary_blocked_numbers WHERE normalizedNumber = :number LIMIT 1")
    suspend fun findByNumber(number: String): TemporaryBlockEntity?

    @Query("SELECT * FROM temporary_blocked_numbers ORDER BY id DESC")
    fun observeAll(): Flow<List<TemporaryBlockEntity>>

    @Query("SELECT * FROM temporary_blocked_numbers WHERE isActive = 1 AND blockedUntil > :now ORDER BY blockedUntil ASC")
    fun observeActive(now: Long): Flow<List<TemporaryBlockEntity>>

    @Query("SELECT * FROM temporary_blocked_numbers WHERE isActive = 1 AND blockedUntil > :now ORDER BY blockedUntil ASC")
    suspend fun activeSnapshot(now: Long): List<TemporaryBlockEntity>

    @Query("UPDATE temporary_blocked_numbers SET isActive = 0 WHERE blockedUntil <= :now AND isActive = 1")
    suspend fun expireOld(now: Long): Int

    @Query("DELETE FROM temporary_blocked_numbers WHERE isActive = 0 AND blockedUntil < :before")
    suspend fun purgeExpired(before: Long)

    @Query("DELETE FROM temporary_blocked_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
