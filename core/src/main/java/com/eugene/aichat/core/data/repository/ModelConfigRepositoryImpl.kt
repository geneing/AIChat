package com.eugene.aichat.core.data.repository

import com.eugene.aichat.core.data.db.dao.ModelConfigDao
import com.eugene.aichat.core.domain.model.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelConfigRepositoryImpl @Inject constructor(
    private val dao: ModelConfigDao
) : ModelConfigRepository {

    override fun observeAll(): Flow<List<ModelConfig>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeByProvider(providerType: String): Flow<List<ModelConfig>> =
        dao.observeByProvider(providerType).map { rows -> rows.map { it.toDomain() } }

    override fun observeDefault(): Flow<ModelConfig?> =
        dao.observeDefault().map { it?.toDomain() }

    override suspend fun getById(id: String): ModelConfig? = dao.getById(id)?.toDomain()

    override suspend fun upsert(model: ModelConfig) {
        val now = System.currentTimeMillis()
        val existing = dao.getById(model.id)
        val createdAt = existing?.createdAt ?: now
        if (model.isDefault) {
            dao.clearDefaults()
        }
        dao.upsert(model.toEntity(createdAt = createdAt, updatedAt = now))
        if (model.isDefault) {
            dao.setDefault(model.id, now)
        }
    }

    override suspend fun setDefault(id: String) {
        val now = System.currentTimeMillis()
        dao.clearDefaults()
        dao.setDefault(id, now)
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}
