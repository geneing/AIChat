package com.eugene.aichat.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skills",
    indices = [Index(value = ["name"], unique = true)]
)
data class SkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val bodyMarkdown: String,
    val tagsCsv: String,
    val toolAllowListCsv: String,
    val isBuiltIn: Boolean = true,
    val isEnabled: Boolean = true,
    val version: Int = 1,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingSync: Boolean = true
)
