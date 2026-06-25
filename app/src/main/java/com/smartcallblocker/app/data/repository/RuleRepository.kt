package com.smartcallblocker.app.data.repository

import com.smartcallblocker.app.data.db.dao.BlockingRuleDao
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val dao: BlockingRuleDao,
) {
    fun observeAll(): Flow<List<BlockingRuleEntity>> = dao.observeAll()
    fun observeEnabled(): Flow<List<BlockingRuleEntity>> = dao.observeEnabled()
    fun countEnabled(): Flow<Int> = dao.countEnabled()

    suspend fun upsert(rule: BlockingRuleEntity): Long {
        return if (rule.id == 0L) dao.insert(rule) else {
            dao.update(rule); rule.id
        }
    }
    suspend fun toggle(rule: BlockingRuleEntity, enabled: Boolean) {
        dao.update(rule.copy(isEnabled = enabled, updatedAt = System.currentTimeMillis()))
    }
    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun get(id: Long): BlockingRuleEntity? = dao.getById(id)
}
