package com.github.geneing.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId"), Index("createdAt")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String,
    val contentText: String?,
    val thinkingText: String? = null,
    val isStreaming: Boolean = false,
    val modelId: String? = null,
    val createdAt: Long,
    val latencyMs: Long? = null,
    val pendingSync: Boolean = true
)
