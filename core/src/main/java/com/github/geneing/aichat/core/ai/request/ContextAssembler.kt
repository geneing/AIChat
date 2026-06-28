package com.github.geneing.aichat.core.ai.request

import com.github.geneing.aichat.core.data.repository.AgentRepository
import com.github.geneing.aichat.core.data.repository.ChatRepository
import com.github.geneing.aichat.core.data.repository.SkillRepository
import com.github.geneing.aichat.core.domain.model.Agent
import com.github.geneing.aichat.core.domain.model.Message
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.Skill
import com.github.geneing.aichat.core.network.dto.ChatMessageDto
import com.github.geneing.aichat.core.network.dto.ChatRequestDto
import com.github.geneing.aichat.core.network.dto.ToolDefinitionDto
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the final [ChatRequestDto] for the AI client. Pulls the
 * system prompt, the enabled skills, the optional agent, and the
 * last N messages from the chat history.
 */
@Singleton
class ContextAssembler @Inject constructor(
    private val skillRepository: SkillRepository,
    private val agentRepository: AgentRepository
) {

    suspend fun build(
        chatId: String,
        chatRepository: ChatRepository,
        model: ModelConfig,
        systemPromptOverride: String? = null,
        agent: Agent? = null,
        enabledSkillIds: Set<String> = emptySet(),
        toolDefinitions: List<ToolDefinitionDto> = emptyList(),
        historyLimit: Int = DEFAULT_HISTORY_LIMIT
    ): ChatRequestDto {
        val system = buildSystemPrompt(systemPromptOverride, agent, enabledSkillIds)
        val history = chatRepository.observeMessages(chatId).first()
            .takeLast(historyLimit)
            .map { it.toDto() }

        val messages = buildList<ChatMessageDto> {
            add(ChatMessageDto(role = "system", content = JsonPrimitive(system)))
            addAll(history)
        }

        val useTools = toolDefinitions.isNotEmpty() && model.supportsTools
        return ChatRequestDto(
            model = model.model,
            messages = messages,
            temperature = model.temperature,
            topP = model.topP,
            maxTokens = model.maxTokens,
            stream = true,
            tools = if (useTools) toolDefinitions else null,
            toolChoice = if (useTools) "auto" else null
        )
    }

    private suspend fun buildSystemPrompt(
        override: String?,
        agent: Agent?,
        enabledSkillIds: Set<String>
    ): String {
        val builder = StringBuilder()
        builder.append(BASE_SYSTEM_PROMPT)
        if (!override.isNullOrBlank()) {
            builder.append("\n\n## User-configured system prompt\n").append(override.trim())
        }
        if (agent != null) {
            builder.append("\n\n## Agent: ").append(agent.name).append('\n')
            builder.append(agent.systemPrompt.trim())
        }
        if (enabledSkillIds.isNotEmpty()) {
            val skills: List<Skill> = enabledSkillIds.mapNotNull { skillRepository.getById(it) }
            if (skills.isNotEmpty()) {
                builder.append("\n\n## Active skills\n")
                for (skill in skills) {
                    builder.append("\n### ").append(skill.name).append('\n')
                    builder.append(skill.body.trim()).append('\n')
                }
            }
        }
        return builder.toString()
    }

    companion object {
        const val DEFAULT_HISTORY_LIMIT = 40

        private val BASE_SYSTEM_PROMPT = """
            You are AIChat, a helpful assistant on an Android device.
            Be concise, use Markdown when appropriate, and cite sources.
            When using tools, prefer the simplest one that answers the question.
        """.trimIndent()
    }
}

private fun Message.toDto(): ChatMessageDto = ChatMessageDto(
    role = role.name.lowercase(),
    content = JsonPrimitive(contentText.orEmpty())
)
