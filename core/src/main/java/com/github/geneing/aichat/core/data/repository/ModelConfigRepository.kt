package com.github.geneing.aichat.core.data.repository

import com.github.geneing.aichat.core.domain.model.ModelConfig
import kotlinx.coroutines.flow.Flow

interface ModelConfigRepository {
    fun observeAll(): Flow<List<ModelConfig>>
    fun observeByProvider(providerType: String): Flow<List<ModelConfig>>
    fun observeDefault(): Flow<ModelConfig?>
    suspend fun getById(id: String): ModelConfig?
    suspend fun upsert(model: ModelConfig)
    suspend fun setDefault(id: String)
    suspend fun deleteById(id: String)
}
