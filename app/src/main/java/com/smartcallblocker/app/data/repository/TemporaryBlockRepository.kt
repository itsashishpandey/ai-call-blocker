package com.smartcallblocker.app.data.repository

import com.smartcallblocker.app.data.db.dao.TemporaryBlockDao
import com.smartcallblocker.app.data.db.entities.TemporaryBlockEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporaryBlockRepository @Inject constructor(
    private val dao: TemporaryBlockDao,
) {
    fun observeActive(now: Long = System.currentTimeMillis()): Flow<List<TemporaryBlockEntity>> =
        dao.observeActive(now)

    suspend fun expireOld(now: Long = System.currentTimeMillis()): Int = dao.expireOld(now)
    suspend fun purgeExpired(before: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000) =
        dao.purgeExpired(before)

    suspend fun delete(id: Long) = dao.deleteById(id)
}
