package com.eugene.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "model_configs",
    indices = [Index("providerType"), Index("isDefault")]
)
data class ModelConfigEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val providerType: String,
    val baseUrl: String,
    val model: String,
    val apiKeyEncrypted: String,
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val maxTokens: Int = 4096,
    val supportsTools: Boolean = true,
    val supportsVision: Boolean = false,
    val supportsAudio: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingSync: Boolean = true
)
