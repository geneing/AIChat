package com.eugene.aichat.core.data.seed

import kotlinx.serialization.Serializable

@Serializable
data class AgentSeed(
    val schemaVersion: Int = 1,
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String = "",
    val modelConfigHint: String? = null,
    val skillIds: List<String> = emptyList(),
    val toolAllowList: List<String> = emptyList(),
    val maxSteps: Int = 6,
    val temperature: Float? = null,
    val version: Int = 1
)
