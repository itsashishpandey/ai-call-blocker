package com.smartcallblocker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcallblocker.app.data.db.entities.WhitelistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: WhitelistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<WhitelistEntity>)

    @Query("DELETE FROM whitelist_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM whitelist_numbers")
    suspend fun deleteAll()

    @Query("SELECT * FROM whitelist_numbers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WhitelistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM whitelist_numbers WHERE normalizedNumber = :number)")
    suspend fun contains(number: String): Boolean

    @Query("SELECT * FROM whitelist_numbers WHERE normalizedNumber LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<WhitelistEntity>>
}
