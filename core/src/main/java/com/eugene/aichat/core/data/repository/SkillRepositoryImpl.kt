package com.eugene.aichat.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.eugene.aichat.core.data.db.dao.SkillDao
import com.eugene.aichat.core.data.seed.AssetSeedLoader
import com.eugene.aichat.core.domain.model.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRepositoryImpl @Inject constructor(
    private val dao: SkillDao,
    private val seedLoader: AssetSeedLoader,
    private val store: DataStore<Preferences>
) : SkillRepository {

    override fun observeAll(): Flow<List<Skill>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeEnabled(): Flow<List<Skill>> =
        dao.observeEnabled().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: String): Skill? = dao.getById(id)?.toDomain()

    override suspend fun upsert(skill: Skill) {
        val now = System.currentTimeMillis()
        val existing = dao.getById(skill.id)
        val createdAt = existing?.createdAt ?: now
        dao.upsert(skill.toEntity(createdAt = createdAt, updatedAt = now))
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        dao.setEnabled(id, enabled, System.currentTimeMillis())
    }

    override suspend fun deleteUserSkill(id: String) {
        dao.deleteUserSkill(id)
    }

    override suspend fun ensureSeeded() {
        val seeded = store.data.first()[Keys.SEEDED] ?: false
        if (seeded) return
        val existing = dao.listIds().toSet()
        val seeds = seedLoader.loadSkills().filter { it.id !in existing }
        seeds.forEach { dao.upsert(it) }
        store.edit { it[Keys.SEEDED] = true }
    }

    private object Keys {
        val SEEDED = booleanPreferencesKey("skills_seeded_v1")
    }
}
