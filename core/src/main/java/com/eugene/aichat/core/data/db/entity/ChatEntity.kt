package com.eugene.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chats",
    indices = [Index("updatedAt"), Index("agentId"), Index("modelConfigId")]
)
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val agentId: String?,
    val modelConfigId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val pendingSync: Boolean = true
)
