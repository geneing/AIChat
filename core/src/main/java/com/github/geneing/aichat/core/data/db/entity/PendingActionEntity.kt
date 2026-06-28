package com.github.geneing.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_actions",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId"), Index("status")]
)
data class PendingActionEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val kind: String,
    val payloadJson: String,
    val status: String,
    val createdAt: Long
)
