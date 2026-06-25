package com.smartcallblocker.app.data.repository

import com.smartcallblocker.app.data.db.dao.BlacklistDao
import com.smartcallblocker.app.data.db.entities.BlacklistEntity
import com.smartcallblocker.app.util.PhoneNumberNormalizer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlacklistRepository @Inject constructor(
    private val dao: BlacklistDao,
    private val normalizer: PhoneNumberNormalizer,
) {
    fun observeAll(): Flow<List<BlacklistEntity>> = dao.observeAll()
    fun search(query: String) = dao.search(query)

    suspend fun add(phoneNumber: String, reason: String?): Long {
        return dao.insert(
            BlacklistEntity(
                phoneNumber = phoneNumber,
                normalizedNumber = normalizer.normalize(phoneNumber),
                reason = reason,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun addAll(numbers: List<String>) {
        val now = System.currentTimeMillis()
        dao.insertAll(numbers.map {
            BlacklistEntity(
                phoneNumber = it,
                normalizedNumber = normalizer.normalize(it),
                reason = null,
                createdAt = now,
            )
        })
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun contains(normalizedNumber: String): Boolean = dao.contains(normalizedNumber)
}
