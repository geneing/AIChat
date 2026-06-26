package com.eugene.aichat.core.data.seed

import android.content.Context
import com.eugene.aichat.core.data.db.entity.AgentEntity
import com.eugene.aichat.core.data.db.entity.SkillEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetSeedLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    suspend fun loadSkills(): List<SkillEntity> {
        val now = System.currentTimeMillis()
        return listAssets("skills").mapNotNull { assetName ->
            runCatching {
                val text = context.assets.open("skills/$assetName").bufferedReader().use { it.readText() }
                val seed = json.decodeFromString(SkillSeed.serializer(), text)
                SkillEntity(
                    id = seed.id,
                    name = seed.name,
                    description = seed.description,
                    systemPrompt = seed.systemPrompt,
                    bodyMarkdown = seed.body,
                    tagsCsv = seed.tags.joinToString(","),
                    toolAllowListCsv = seed.toolAllowList.joinToString(","),
                    isBuiltIn = true,
                    isEnabled = true,
                    version = seed.version,
                    createdAt = now,
                    updatedAt = now,
                    pendingSync = true
                )
            }.getOrNull()
        }
    }

    suspend fun loadAgents(): List<AgentEntity> {
        val now = System.currentTimeMillis()
        return listAssets("agents").mapNotNull { assetName ->
            runCatching {
                val text = context.assets.open("agents/$assetName").bufferedReader().use { it.readText() }
                val seed = json.decodeFromString(AgentSeed.serializer(), text)
                AgentEntity(
                    id = seed.id,
                    name = seed.name,
                    description = seed.description,
                    systemPrompt = seed.systemPrompt,
                    modelConfigHint = seed.modelConfigHint,
                    modelConfigId = null,
                    skillIdsCsv = seed.skillIds.joinToString(","),
                    toolAllowListCsv = seed.toolAllowList.joinToString(","),
                    maxSteps = seed.maxSteps,
                    temperature = seed.temperature,
                    isBuiltIn = true,
                    isEnabled = true,
                    version = seed.version,
                    createdAt = now,
                    updatedAt = now,
                    pendingSync = true
                )
            }.getOrNull()
        }
    }

    private fun listAssets(folder: String): List<String> = runCatching {
        context.assets.list(folder)?.toList()?.filter { it.endsWith(".json") } ?: emptyList()
    }.getOrDefault(emptyList())
}
