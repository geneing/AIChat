package com.eugene.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "source_refs",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId")]
)
data class SourceRefEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val title: String?,
    val url: String,
    val snippet: String?
)
