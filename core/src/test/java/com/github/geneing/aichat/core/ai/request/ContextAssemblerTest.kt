package com.github.geneing.aichat.core.ai.request

import com.github.geneing.aichat.core.data.db.dao.AgentDao
import com.github.geneing.aichat.core.data.db.dao.ChatDao
import com.github.geneing.aichat.core.data.db.dao.MessageDao
import com.github.geneing.aichat.core.data.db.entity.AgentEntity
import com.github.geneing.aichat.core.data.db.entity.ChatEntity
import com.github.geneing.aichat.core.data.db.entity.MessageEntity
import com.github.geneing.aichat.core.data.db.entity.SkillEntity
import com.github.geneing.aichat.core.data.repository.AgentRepository
import com.github.geneing.aichat.core.data.repository.AgentRepositoryImpl
import com.github.geneing.aichat.core.data.repository.ChatRepository
import com.github.geneing.aichat.core.data.repository.ChatRepositoryImpl
import com.github.geneing.aichat.core.data.repository.SkillRepository
import com.github.geneing.aichat.core.data.repository.SkillRepositoryImpl
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.ProviderType
import com.github.geneing.aichat.core.data.seed.AssetSeedLoader
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ContextAssemblerTest {

    private fun newModel() = ModelConfig(
        id = "m1", displayName = "gpt-4o", providerType = ProviderType.OPENAI,
        baseUrl = "https://api.openai.com/v1", model = "gpt-4o", apiKey = "k",
        temperature = 0.5f, topP = 1f, maxTokens = 1024,
        supportsTools = true, supportsVision = false, supportsAudio = false, isDefault = false
    )

    @Test
    fun `system prompt includes base, agent, and skills`() = runTest {
        val skillRepo = FakeSkillRepo(
            listOf(
                SkillEntity(
                    id = "skill.test", name = "Test", description = "d",
                    systemPrompt = "sp", bodyMarkdown = "INSTRUCT",
                    tagsCsv = "", toolAllowListCsv = "", isBuiltIn = true,
                    isEnabled = true, version = 1, createdAt = 0, updatedAt = 0
                )
            )
        )
        val agentRepo = FakeAgentRepo(emptyList())
        val chatRepo = mockk<ChatRepository>(relaxed = true) {
            every { observeMessages("c1") } returns flowOf(emptyList())
        }
        val assembler = ContextAssembler(skillRepo, agentRepo)

        val req = assembler.build(
            chatId = "c1",
            chatRepository = chatRepo,
            model = newModel(),
            agent = null,
            enabledSkillIds = setOf("skill.test")
        )

        val system = req.messages.first().content?.toString().orEmpty()
        assertThat(system).contains("AIChat")
        assertThat(system).contains("INSTRUCT")
    }

    @Test
    fun `tools are forwarded only when model supports them`() = runTest {
        val assembler = ContextAssembler(FakeSkillRepo(emptyList()), FakeAgentRepo(emptyList()))
        val chatRepo = mockk<ChatRepository>(relaxed = true) {
            every { observeMessages("c1") } returns flowOf(emptyList())
        }
        val tools = listOf(
            com.github.geneing.aichat.core.network.dto.ToolDefinitionDto(
                function = com.github.geneing.aichat.core.network.dto.ToolFunctionDto(
                    name = "demo",
                    description = "d",
                    parameters = kotlinx.serialization.json.buildJsonObject {}
                )
            )
        )

        val withTools = assembler.build(
            chatId = "c1",
            chatRepository = chatRepo,
            model = newModel().copy(supportsTools = true),
            toolDefinitions = tools
        )
        assertThat(withTools.tools).isNotNull()
        assertThat(withTools.tools).hasSize(1)

        val withoutTools = assembler.build(
            chatId = "c1",
            chatRepository = chatRepo,
            model = newModel().copy(supportsTools = false),
            toolDefinitions = tools
        )
        assertThat(withoutTools.tools).isNull()
    }
}

private class FakeSkillRepo(initial: List<SkillEntity>) : SkillRepository {
    private val store = initial.associateBy { it.id }.toMutableMap()
    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<com.github.geneing.aichat.core.domain.model.Skill>> =
        MutableStateFlow(store.values.map { it.toDomain() })
    override fun observeEnabled(): kotlinx.coroutines.flow.Flow<List<com.github.geneing.aichat.core.domain.model.Skill>> =
        MutableStateFlow(store.values.filter { it.isEnabled }.map { it.toDomain() })
    override suspend fun getById(id: String) = store[id]?.toDomain()
    override suspend fun upsert(skill: com.github.geneing.aichat.core.domain.model.Skill) = Unit
    override suspend fun setEnabled(id: String, enabled: Boolean) = Unit
    override suspend fun deleteUserSkill(id: String) = Unit
    override suspend fun ensureSeeded() = Unit
}

private fun SkillEntity.toDomain(): com.github.geneing.aichat.core.domain.model.Skill =
    com.github.geneing.aichat.core.domain.model.Skill(
        id = id, name = name, description = description,
        systemPrompt = systemPrompt, body = bodyMarkdown,
        tags = tagsCsv.splitCsv(), toolAllowList = toolAllowListCsv.splitCsv(),
        isBuiltIn = isBuiltIn, isEnabled = isEnabled, version = version
    )

private fun String.splitCsv(): List<String> =
    if (isBlank()) emptyList() else split(",").map { it.trim() }.filter { it.isNotEmpty() }

private class FakeAgentRepo(initial: List<AgentEntity>) : AgentRepository {
    private val store = initial.associateBy { it.id }.toMutableMap()
    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<com.github.geneing.aichat.core.domain.model.Agent>> =
        MutableStateFlow(store.values.map { it.toDomain() })
    override fun observeEnabled(): kotlinx.coroutines.flow.Flow<List<com.github.geneing.aichat.core.domain.model.Agent>> =
        MutableStateFlow(store.values.filter { it.isEnabled }.map { it.toDomain() })
    override suspend fun getById(id: String) = store[id]?.toDomain()
    override suspend fun upsert(agent: com.github.geneing.aichat.core.domain.model.Agent) = Unit
    override suspend fun setEnabled(id: String, enabled: Boolean) = Unit
    override suspend fun deleteUserAgent(id: String) = Unit
    override suspend fun ensureSeeded() = Unit
}

private fun AgentEntity.toDomain(): com.github.geneing.aichat.core.domain.model.Agent =
    com.github.geneing.aichat.core.domain.model.Agent(
        id = id, name = name, description = description,
        systemPrompt = systemPrompt, modelConfigHint = modelConfigHint,
        modelConfigId = modelConfigId, skillIds = skillIdsCsv.splitCsv(),
        toolAllowList = toolAllowListCsv.splitCsv(), maxSteps = maxSteps,
        temperature = temperature, isBuiltIn = isBuiltIn,
        isEnabled = isEnabled, version = version
    )
