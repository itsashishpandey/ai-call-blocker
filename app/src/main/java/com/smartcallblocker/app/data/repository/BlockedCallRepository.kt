package com.smartcallblocker.app.data.repository

import com.smartcallblocker.app.data.db.dao.BlockedCallDao
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedCallRepository @Inject constructor(
    private val dao: BlockedCallDao,
) {
    suspend fun log(entity: BlockedCallEntity): Long = dao.insert(entity)

    fun observeRecent(limit: Int = 200): Flow<List<BlockedCallEntity>> = dao.observeRecent(limit)

    fun observeAll(): Flow<List<BlockedCallEntity>> = dao.observeAll()

    fun observeByNumber(number: String) = dao.observeByNumber(number)

    fun countSince(since: Long): Flow<Int> = dao.countSince(since)
    fun countAll(): Flow<Int> = dao.countAll()
    fun observeLatest(): Flow<BlockedCallEntity?> = dao.observeLatest()

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun deleteOlderThan(before: Long) = dao.deleteOlderThan(before)
}
