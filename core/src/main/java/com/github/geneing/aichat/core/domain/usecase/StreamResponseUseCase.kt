package com.github.geneing.aichat.core.domain.usecase

import com.github.geneing.aichat.core.ai.client.AiClient
import com.github.geneing.aichat.core.ai.request.ContextAssembler
import com.github.geneing.aichat.core.ai.response.StreamEvent
import com.github.geneing.aichat.core.ai.tools.ActionDispatcher
import com.github.geneing.aichat.core.ai.tools.DefaultToolContext
import com.github.geneing.aichat.core.ai.tools.ToolContext
import com.github.geneing.aichat.core.ai.tools.ToolRegistry
import com.github.geneing.aichat.core.data.repository.AgentRepository
import com.github.geneing.aichat.core.data.repository.ChatRepository
import com.github.geneing.aichat.core.data.repository.SkillRepository
import com.github.geneing.aichat.core.domain.model.Agent
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.SourceRef
import com.github.geneing.aichat.core.network.dto.ChatMessageDto
import com.github.geneing.aichat.core.network.dto.ToolCallDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID
import javax.inject.Inject

/**
 * Streams a model response for a given chat, accumulating text into the
 * assistant message and emitting incremental [StreamEvent]s.
 *
 * The runtime resolves the chat's agent (if any) and the user's enabled
 * skills, builds a [com.github.geneing.aichat.core.network.dto.ChatRequestDto]
 * with the right tool definitions, then drives a tool-use loop:
 *
 *   1. Call the model.
 *   2. If the response contains tool calls, execute them via
 *      [ToolRegistry], record them via [ActionDispatcher], and feed the
 *      results back as `role: "tool"` messages.
 *   3. Loop until the model returns without tool calls, or [maxSteps]
 *      is reached (per agent, or [DEFAULT_MAX_STEPS] otherwise).
 *
 * The caller is responsible for cancelling the returned [Flow] to abort
 * the in-flight request (e.g. on user interrupt).
 */
class StreamResponseUseCase @Inject constructor(
    private val aiClient: AiClient,
    private val contextAssembler: ContextAssembler,
    private val chatRepository: ChatRepository,
    private val toolRegistry: ToolRegistry,
    private val skillRepository: SkillRepository,
    private val agentRepository: AgentRepository,
    private val actionDispatcher: ActionDispatcher,
    private val defaultToolContext: DefaultToolContext
) {
    operator fun invoke(
        chatId: String,
        model: ModelConfig,
        assistantMessageId: String,
        onComplete: (() -> Unit)? = null
    ): Flow<StreamEvent> = flow {
        val started = System.currentTimeMillis()
        var finalText = ""
        var finalThinking = ""
        var errored = false

        try {
            val (agent, enabledSkillIds, toolAllowList) = resolveAgentAndSkills(chatId)
            val maxSteps = (agent?.maxSteps ?: 0).coerceAtLeast(0)
                .takeIf { it > 0 } ?: DEFAULT_MAX_STEPS

            val initialRequest = contextAssembler.build(
                chatId = chatId,
                chatRepository = chatRepository,
                model = model,
                agent = agent,
                enabledSkillIds = enabledSkillIds,
                toolDefinitions = toolRegistry.definitions(),
                toolAllowList = toolAllowList
            )

            val workingMessages = initialRequest.messages.toMutableList()
            val toolContext = ChatToolContext(defaultToolContext, chatId)
            val baseRequest = initialRequest.copy(messages = workingMessages)
            val collectedSources = mutableListOf<SourceRef>()

            for (step in 1..maxSteps) {
                val stepRequest = baseRequest.copy(messages = workingMessages.toList())
                val collectedText = StringBuilder()
                val collectedToolCalls = mutableListOf<ToolCallDto>()
                var stepFailed: String? = null

                aiClient.stream(model, stepRequest)
                    .collect { ev ->
                        when (ev) {
                            is StreamEvent.ContentDelta -> {
                                collectedText.append(ev.text)
                                finalText += ev.text
                                chatRepository.updateMessageContent(
                                    messageId = assistantMessageId,
                                    text = finalText,
                                    streaming = true
                                )
                            }
                            is StreamEvent.ThinkingDelta -> {
                                finalThinking += ev.text
                                chatRepository.updateMessageThinking(
                                    messageId = assistantMessageId,
                                    text = finalThinking
                                )
                            }
                            is StreamEvent.ToolCallsReady -> {
                                collectedToolCalls.addAll(ev.calls)
                            }
                            is StreamEvent.Failed -> {
                                stepFailed = ev.message
                            }
                            else -> Unit
                        }
                        emit(ev)
                    }

                if (stepFailed != null) {
                    errored = true
                    chatRepository.updateMessageContent(
                        messageId = assistantMessageId,
                        text = "⚠️ $stepFailed",
                        streaming = false
                    )
                    chatRepository.markMessageComplete(assistantMessageId, null)
                    break
                }

                if (collectedToolCalls.isEmpty()) break

                workingMessages += ChatMessageDto(
                    role = "assistant",
                    content = JsonPrimitive(collectedText.toString()),
                    toolCalls = collectedToolCalls.toList()
                )

                for (call in collectedToolCalls) {
                    emit(StreamEvent.ToolCallSummary(call.function.name))
                    val result = runCatching { toolRegistry.execute(call, toolContext) }
                        .getOrElse {
                            com.github.geneing.aichat.core.ai.tools.ToolResult.Error(
                                it.message ?: "tool failed"
                            )
                        }
                    actionDispatcher.recordToolCall(assistantMessageId, call, result)
                    emit(StreamEvent.ToolResult(call.function.name, result.content))
                    if (call.function.name == "web_search") {
                        collectSourcesFromWebSearch(result.content, collectedSources)
                    }
                    workingMessages += ChatMessageDto(
                        role = "tool",
                        content = JsonPrimitive(result.content),
                        toolCallId = call.id,
                        name = call.function.name
                    )
                }
            }

            if (!errored) {
                chatRepository.updateMessageContent(
                    messageId = assistantMessageId,
                    text = finalText.ifBlank { null },
                    streaming = false
                )
                if (finalThinking.isNotEmpty()) {
                    chatRepository.updateMessageThinking(
                        messageId = assistantMessageId,
                        text = finalThinking
                    )
                }
                chatRepository.markMessageComplete(
                    assistantMessageId,
                    System.currentTimeMillis() - started
                )
                if (finalText.isNotBlank()) {
                    actionDispatcher.scanFinalText(assistantMessageId, finalText)
                }
                if (collectedSources.isNotEmpty()) {
                    chatRepository.addSources(assistantMessageId, collectedSources.distinctBy { it.url })
                }
                onComplete?.invoke()
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            chatRepository.updateMessageContent(
                messageId = assistantMessageId,
                text = "⚠️ ${e.message ?: "Unknown error"}",
                streaming = false
            )
            chatRepository.markMessageComplete(assistantMessageId, null)
            emit(StreamEvent.Failed(e.message ?: "Unknown error"))
        }
    }.catch { e ->
        chatRepository.updateMessageContent(
            messageId = assistantMessageId,
            text = "⚠️ ${e.message ?: "Unknown error"}",
            streaming = false
        )
        chatRepository.markMessageComplete(assistantMessageId, null)
        emit(StreamEvent.Failed(e.message ?: "Unknown error"))
    }

    private suspend fun resolveAgentAndSkills(chatId: String): Triple<Agent?, Set<String>, List<String>> {
        val chat = chatRepository.getChat(chatId)
        val agent = chat?.agentId?.let { agentRepository.getById(it) }
        val skillIds: Set<String> = if (agent != null && agent.skillIds.isNotEmpty()) {
            agent.skillIds.toSet()
        } else {
            skillRepository.observeEnabled().first().map { it.id }.toSet()
        }
        val toolAllowList: List<String> = agent?.toolAllowList.orEmpty()
        return Triple(agent, skillIds, toolAllowList)
    }

    /**
     * Parses the `web_search` tool's result body, which the
     * [com.github.geneing.aichat.core.ai.tools.WebSearchTool] formats as
     * repeated `title\nurl\nsnippet` triples separated by blank lines.
     * Extracted sources are appended to [out].
     */
    private fun collectSourcesFromWebSearch(content: String, out: MutableList<SourceRef>) {
        if (content.isBlank()) return
        val blocks = content.split("\n\n")
        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.size < 2) continue
            val title = lines[0]
            val url = lines[1]
            if (!url.startsWith("http://") && !url.startsWith("https://")) continue
            val snippet = if (lines.size >= 3) lines.drop(2).joinToString(" ") else null
            out += SourceRef(
                id = UUID.randomUUID().toString(),
                messageId = "",
                title = title,
                url = url,
                snippet = snippet
            )
        }
    }

    companion object {
        const val DEFAULT_MAX_STEPS = 4
    }
}

/**
 * Per-chat [ToolContext] that delegates everything to a shared
 * [DefaultToolContext] but pins the current [chatId] so tools that
 * need it (e.g. for logging, file access, or future per-chat
 * authorization) can read it.
 */
private class ChatToolContext(
    private val delegate: DefaultToolContext,
    private val chatIdValue: String
) : ToolContext {
    override val caller get() = delegate.caller
    override val chatId: String? get() = chatIdValue
    override val locationProvider get() = delegate.locationProvider
    override val webSearchProvider get() = delegate.webSearchProvider
}
