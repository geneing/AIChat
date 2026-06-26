package com.eugene.aichat.core.data.repository

import com.eugene.aichat.core.domain.model.Skill
import kotlinx.coroutines.flow.Flow

interface SkillRepository {
    fun observeAll(): Flow<List<Skill>>
    fun observeEnabled(): Flow<List<Skill>>
    suspend fun getById(id: String): Skill?
    suspend fun upsert(skill: Skill)
    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun deleteUserSkill(id: String)
    suspend fun ensureSeeded()
}
