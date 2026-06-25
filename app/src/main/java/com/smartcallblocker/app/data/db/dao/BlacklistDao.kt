package com.smartcallblocker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcallblocker.app.data.db.entities.BlacklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BlacklistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<BlacklistEntity>)

    @Query("DELETE FROM blacklist_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM blacklist_numbers")
    suspend fun deleteAll()

    @Query("SELECT * FROM blacklist_numbers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BlacklistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM blacklist_numbers WHERE normalizedNumber = :number)")
    suspend fun contains(number: String): Boolean

    @Query("SELECT * FROM blacklist_numbers WHERE normalizedNumber LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<BlacklistEntity>>
}
