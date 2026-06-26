package com.eugene.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agents",
    indices = [Index(value = ["name"], unique = true)]
)
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val modelConfigHint: String?,
    val modelConfigId: String?,
    val skillIdsCsv: String,
    val toolAllowListCsv: String,
    val maxSteps: Int = 6,
    val temperature: Float? = null,
    val isBuiltIn: Boolean = true,
    val isEnabled: Boolean = true,
    val version: Int = 1,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingSync: Boolean = true
)
