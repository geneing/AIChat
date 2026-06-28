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
        toolAllowList: List<String> = emptyList(),
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

        val effectiveTools = if (toolAllowList.isEmpty()) {
            toolDefinitions
        } else {
            toolDefinitions.filter { it.function.name in toolAllowList }
        }
        val useTools = effectiveTools.isNotEmpty() && model.supportsTools
        return ChatRequestDto(
            model = model.model,
            messages = messages,
            temperature = model.temperature,
            topP = model.topP,
            maxTokens = model.maxTokens,
            stream = true,
            tools = if (useTools) effectiveTools else null,
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

            ## Behavior
            - Be concise, friendly, and direct. Prefer short paragraphs and
              bullet lists over walls of text.
            - Use Markdown (headings, lists, code fences, bold/italic) when
              it improves clarity.
            - Think out loud only when the user asks you to. Otherwise jump
              straight to the answer.
            - If you are unsure about a fact, say so. Do not invent URLs,
              citations, prices, dates, or people.

            ## Tool use (very important)
            You have access to tools. The runtime runs a loop: you may emit
            one or more tool calls, the runtime executes them and feeds the
            results back as `role: "tool"` messages, then you can continue.
            This means:

            - When the question requires live data (prices, weather, news,
              recent events, anything time-sensitive), call `web_search`
              first. Do not guess.
            - When the user asks for "near me", "where I am", or directions,
              call `get_location` and then `open_maps` with the result (or
              a place name).
            - When the user asks for the current time, locale, or time zone,
              call `get_local_info`.
            - When the user asks you to open a webpage, a video, a repo, or
              any other URL, call `open_url` with the full absolute URL.
            - When the user asks to open a place on a map, call `open_maps`.
            - Prefer the simplest tool that answers the question. Do not
              call a tool for something you already know.
            - Tools can fail. If a tool returns an error or "not configured",
              fall back gracefully: tell the user what happened and suggest
              a manual action (e.g. "Web search is not configured; add a
              SerpAPI key in Settings → Web Search to enable it.").
            - You may call several tools in parallel when there are no
              dependencies between them (e.g. web_search + get_location).
            - Keep tool arguments strictly to the JSON schema. Do not add
              fields the schema does not declare.

            ## URLs and links (critical for the Android UI)
            The chat client renders Markdown. **Every URL you mention MUST
            be a Markdown link of the form `[descriptive title](https://...)`.**
            Bare URLs in plain text will still be auto-linked, but they are
            ugly and unreadable. Follow these rules:

            - For every webpage you cite, write
              `[Page title — short hint](https://example.com/full-url)`.
              The "descriptive title" should be 2–8 words and tell the user
              what the link is, not the raw domain.
            - For search results returned by `web_search`, prefer
              `[1] Page title — https://example.com/...` style and put the
              matching numbers in your text ("…as reported in [1] and [2].").
            - Do not invent or shorten URLs. Copy them verbatim from the
              tool result.
            - For places on a map, write
              `[Open in Maps](geo:0,0?q=encoded+address)` or use the
              `open_maps` tool — the client also recognizes `https://maps...`
              links and makes them tappable.
            - For emails, use `[name](mailto:...)`. For phones, use
              `[name](tel:...)`. The client renders all of these as
              tappable links that hand off to the system handler.

            After your final answer the client scans the response for any
            remaining bare http(s) URLs and `geo:` URIs and turns them into
            tappable chips, so the Markdown link format is preferred but
            not strictly required for the very last URL you mention.

            ## Sources and citations
            - When you draw on a search result, attribute it inline with
              the numbered link form above ("…per [1].").
            - When you paraphrase a tool result, do not pretend it is
              common knowledge. Cite the tool.
            - Do not fabricate sources. If you did not search, do not cite.

            ## Persona and skills
            - Sections labeled `## Agent:` and `## Active skills:` are
              appended below this prompt. Follow them strictly.
            - If they conflict with anything above, the agent / skills
              instructions win.
        """.trimIndent()
    }
}

private fun Message.toDto(): ChatMessageDto = ChatMessageDto(
    role = role.name.lowercase(),
    content = JsonPrimitive(contentText.orEmpty())
)
