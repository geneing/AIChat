package com.github.geneing.aichat.core.data.seed

import kotlinx.serialization.Serializable

@Serializable
data class SkillSeed(
    val schemaVersion: Int = 1,
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String = "",
    val body: String = "",
    val tags: List<String> = emptyList(),
    val toolAllowList: List<String> = emptyList(),
    val version: Int = 1
)
