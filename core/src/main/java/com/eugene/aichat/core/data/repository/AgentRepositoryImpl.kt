package com.eugene.aichat.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.eugene.aichat.core.data.db.dao.AgentDao
import com.eugene.aichat.core.data.seed.AssetSeedLoader
import com.eugene.aichat.core.domain.model.Agent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentRepositoryImpl @Inject constructor(
    private val dao: AgentDao,
    private val seedLoader: AssetSeedLoader,
    private val store: DataStore<Preferences>
) : AgentRepository {

    override fun observeAll(): Flow<List<Agent>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeEnabled(): Flow<List<Agent>> =
        dao.observeEnabled().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: String): Agent? = dao.getById(id)?.toDomain()

    override suspend fun upsert(agent: Agent) {
        val now = System.currentTimeMillis()
        val existing = dao.getById(agent.id)
        val createdAt = existing?.createdAt ?: now
        dao.upsert(agent.toEntity(createdAt = createdAt, updatedAt = now))
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        dao.setEnabled(id, enabled, System.currentTimeMillis())
    }

    override suspend fun deleteUserAgent(id: String) {
        dao.deleteUserAgent(id)
    }

    override suspend fun ensureSeeded() {
        val seeded = store.data.first()[Keys.SEEDED] ?: false
        if (seeded) return
        val existing = dao.listIds().toSet()
        val seeds = seedLoader.loadAgents().filter { it.id !in existing }
        seeds.forEach { dao.upsert(it) }
        store.edit { it[Keys.SEEDED] = true }
    }

    private object Keys {
        val SEEDED = booleanPreferencesKey("agents_seeded_v1")
    }
}
