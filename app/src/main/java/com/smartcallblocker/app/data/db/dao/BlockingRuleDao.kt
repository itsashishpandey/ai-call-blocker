package com.smartcallblocker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockingRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: BlockingRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<BlockingRuleEntity>)

    @Query("DELETE FROM blocking_rules")
    suspend fun deleteAll()

    @Update
    suspend fun update(rule: BlockingRuleEntity)

    @Delete
    suspend fun delete(rule: BlockingRuleEntity)

    @Query("DELETE FROM blocking_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM blocking_rules ORDER BY priority ASC, id ASC")
    fun observeAll(): Flow<List<BlockingRuleEntity>>

    @Query("SELECT * FROM blocking_rules WHERE isEnabled = 1 ORDER BY priority ASC, id ASC")
    fun observeEnabled(): Flow<List<BlockingRuleEntity>>

    @Query("SELECT * FROM blocking_rules WHERE isEnabled = 1 ORDER BY priority ASC, id ASC")
    suspend fun enabledSnapshot(): List<BlockingRuleEntity>

    @Query("SELECT COUNT(*) FROM blocking_rules WHERE isEnabled = 1")
    fun countEnabled(): Flow<Int>

    @Query("SELECT * FROM blocking_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BlockingRuleEntity?
}
