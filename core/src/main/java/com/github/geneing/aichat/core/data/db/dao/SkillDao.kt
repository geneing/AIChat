package com.github.geneing.aichat.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.geneing.aichat.core.data.db.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {

    @Query("SELECT * FROM skills ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE isEnabled = 1 ORDER BY name ASC")
    fun observeEnabled(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SkillEntity?

    @Query("SELECT id FROM skills")
    suspend fun listIds(): List<String>

    @Upsert
    suspend fun upsert(skill: SkillEntity)

    @Query("UPDATE skills SET isEnabled = :enabled, updatedAt = :ts WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean, ts: Long)

    @Query("DELETE FROM skills WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteUserSkill(id: String)
}
