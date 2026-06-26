package com.eugene.aichat.core.data.repository

import com.eugene.aichat.core.data.db.dao.ModelConfigDao
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.security.EncryptedKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelConfigRepositoryImpl @Inject constructor(
    private val dao: ModelConfigDao,
    private val encryptedKeyStore: EncryptedKeyStore
) : ModelConfigRepository {

    override fun observeAll(): Flow<List<ModelConfig>> =
        dao.observeAll().map { rows -> rows.map { it.toDomainWithKey(encryptedKeyStore) } }

    override fun observeByProvider(providerType: String): Flow<List<ModelConfig>> =
        dao.observeByProvider(providerType).map { rows -> rows.map { it.toDomainWithKey(encryptedKeyStore) } }

    override fun observeDefault(): Flow<ModelConfig?> =
        dao.observeDefault().map { it?.toDomainWithKey(encryptedKeyStore) }

    override suspend fun getById(id: String): ModelConfig? =
        dao.getById(id)?.toDomainWithKey(encryptedKeyStore)

    override suspend fun upsert(model: ModelConfig) {
        val now = System.currentTimeMillis()
        val existing = dao.getById(model.id)
        val createdAt = existing?.createdAt ?: now
        if (model.isDefault) {
            dao.clearDefaults()
        }
        encryptedKeyStore.putApiKey(model.id, model.apiKey)
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
        encryptedKeyStore.removeApiKey(id)
        dao.deleteById(id)
    }
}

private fun com.eugene.aichat.core.data.db.entity.ModelConfigEntity.toDomainWithKey(
    store: EncryptedKeyStore
): ModelConfig = toDomain(apiKey = store.getApiKey(id))
