package com.github.geneing.aichat.core.ai.agents

import com.github.geneing.aichat.core.ai.response.StreamEvent
import com.github.geneing.aichat.core.ai.tools.ActionDispatcher
import com.github.geneing.aichat.core.ai.tools.ToolContext
import com.github.geneing.aichat.core.ai.tools.ToolRegistry
import com.github.geneing.aichat.core.ai.tools.ToolResult
import com.github.geneing.aichat.core.domain.model.Agent
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.network.dto.ChatMessageDto
import com.github.geneing.aichat.core.network.dto.ChatRequestDto
import com.github.geneing.aichat.core.network.dto.ToolCallDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-step agentic loop. For each step:
 *  1. Call the LLM.
 *  2. If the response contains tool_calls, execute them and append
 *     the results as tool messages.
 *  3. Repeat until the LLM returns without tool_calls, or [maxSteps]
 *     is reached.
 *
 * Each step is emitted as a [Step] event for the UI to render a
 * progress indicator.
 */
@Singleton
class AgentRuntime @Inject constructor(
    private val toolRegistry: ToolRegistry,
    private val actionDispatcher: ActionDispatcher
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Drives an agent conversation. The caller supplies the streaming
     * [modelStream] function so this class stays decoupled from the
     * concrete [com.github.geneing.aichat.core.ai.client.AiClient] (so the
     * runtime can be unit-tested with a fake).
     */
    fun run(
        agent: Agent,
        model: ModelConfig,
        initialMessages: List<ChatMessageDto>,
        toolContext: ToolContext,
        messageId: String,
        modelStream: suspend (ChatRequestDto) -> Flow<StreamEvent>
    ): Flow<Step> = flow {
        val messages = initialMessages.toMutableList()
        val tools = toolRegistry.definitions()
        var stepIndex = 0

        while (stepIndex < agent.maxSteps) {
            stepIndex++
            emit(Step.Thinking(stepIndex, agent.maxSteps))

            val request = ChatRequestDto(
                model = model.model,
                messages = messages,
                temperature = agent.temperature ?: model.temperature,
                stream = true,
                tools = tools.takeIf { model.supportsTools },
                toolChoice = if (model.supportsTools) "auto" else null
            )

            val collectedText = StringBuilder()
            val collectedToolCalls = mutableListOf<ToolCallDto>()
            var failed: String? = null

            modelStream(request).collect { ev ->
                when (ev) {
                    is StreamEvent.ContentDelta -> collectedText.append(ev.text)
                    is StreamEvent.ThinkingDelta -> { /* already represented upstream */ }
                    is StreamEvent.ToolCallDelta -> {
                        val builder = collectedToolCalls
                            .getOrNull(collectedToolCalls.size - 1)
                            ?.takeIf { it.id == ev.call.id }
                        if (builder == null) {
                            collectedToolCalls += ToolCallDto(
                                id = ev.call.id ?: "",
                                function = com.github.geneing.aichat.core.network.dto.ToolCallFunctionDto(
                                    name = ev.call.function?.name.orEmpty(),
                                    arguments = ""
                                )
                            )
                        }
                    }
                    is StreamEvent.ToolCallsReady -> {
                        collectedToolCalls.clear()
                        collectedToolCalls.addAll(ev.calls)
                    }
                    StreamEvent.Completed -> Unit
                    is StreamEvent.Failed -> failed = ev.message
                }
            }

            if (failed != null) {
                emit(Step.Failed(failed))
                return@flow
            }

            // Persist any URLs / geo URIs we saw in the final text so the UI
            // can offer taps.
            if (collectedText.isNotEmpty()) {
                actionDispatcher.scanFinalText(messageId, collectedText.toString())
            }

            if (collectedToolCalls.isEmpty()) {
                emit(Step.FinalAnswer(collectedText.toString()))
                return@flow
            }

            // Append the assistant message + tool result messages for next step.
            messages += ChatMessageDto(
                role = "assistant",
                content = kotlinx.serialization.json.JsonPrimitive(collectedText.toString()),
                toolCalls = collectedToolCalls
            )

            for (call in collectedToolCalls) {
                emit(Step.ToolInvocation(call.function.name))
                val result: ToolResult = toolRegistry.execute(call, toolContext)
                actionDispatcher.recordToolCall(messageId, call, result)
                messages += ChatMessageDto(
                    role = "tool",
                    content = kotlinx.serialization.json.JsonPrimitive(result.content),
                    toolCallId = call.id,
                    name = call.function.name
                )
            }
        }

        emit(Step.FinalAnswer("(step limit reached)"))
    }

    /**
     * Smaller helper for emitting a tool message when the model returns
     * tool_calls but the agent has not been configured. Useful as a
     * safety net to keep the conversation moving.
     */
    fun toolOnlyMessage(callId: String, name: String, result: ToolResult): ChatMessageDto =
        ChatMessageDto(
            role = "tool",
            content = JsonPrimitive(result.content),
            toolCallId = callId,
            name = name
        )
}

/** UI-facing events emitted by the agent loop. */
sealed interface Step {
    data class Thinking(val step: Int, val max: Int) : Step
    data class ToolInvocation(val name: String) : Step
    data class FinalAnswer(val text: String) : Step
    data class Failed(val message: String) : Step
}
