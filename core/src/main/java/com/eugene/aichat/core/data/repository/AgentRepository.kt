package com.eugene.aichat.core.data.repository

import com.eugene.aichat.core.domain.model.Agent
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    fun observeAll(): Flow<List<Agent>>
    fun observeEnabled(): Flow<List<Agent>>
    suspend fun getById(id: String): Agent?
    suspend fun upsert(agent: Agent)
    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun deleteUserAgent(id: String)
    suspend fun ensureSeeded()
}
