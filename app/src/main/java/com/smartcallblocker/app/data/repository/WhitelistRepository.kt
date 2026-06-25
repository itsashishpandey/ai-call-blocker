package com.smartcallblocker.app.data.repository

import com.smartcallblocker.app.data.db.dao.WhitelistDao
import com.smartcallblocker.app.data.db.entities.WhitelistEntity
import com.smartcallblocker.app.util.PhoneNumberNormalizer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhitelistRepository @Inject constructor(
    private val dao: WhitelistDao,
    private val normalizer: PhoneNumberNormalizer,
) {
    fun observeAll(): Flow<List<WhitelistEntity>> = dao.observeAll()
    fun search(query: String) = dao.search(query)

    suspend fun add(phoneNumber: String, name: String?, source: String = "MANUAL"): Long {
        val normalized = normalizer.normalize(phoneNumber)
        return dao.insert(
            WhitelistEntity(
                phoneNumber = phoneNumber,
                normalizedNumber = normalized,
                name = name,
                source = source,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun addAll(items: List<Pair<String, String?>>) {
        val now = System.currentTimeMillis()
        dao.insertAll(items.map { (num, name) ->
            WhitelistEntity(
                phoneNumber = num,
                normalizedNumber = normalizer.normalize(num),
                name = name,
                source = "CONTACTS",
                createdAt = now,
            )
        })
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun contains(normalizedNumber: String): Boolean = dao.contains(normalizedNumber)
}
