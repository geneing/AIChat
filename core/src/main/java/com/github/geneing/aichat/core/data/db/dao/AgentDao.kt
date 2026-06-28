package com.github.geneing.aichat.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.geneing.aichat.core.data.db.entity.AgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {

    @Query("SELECT * FROM agents ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE isEnabled = 1 ORDER BY name ASC")
    fun observeEnabled(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AgentEntity?

    @Query("SELECT id FROM agents")
    suspend fun listIds(): List<String>

    @Upsert
    suspend fun upsert(agent: AgentEntity)

    @Query("UPDATE agents SET isEnabled = :enabled, updatedAt = :ts WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean, ts: Long)

    @Query("DELETE FROM agents WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteUserAgent(id: String)
}
