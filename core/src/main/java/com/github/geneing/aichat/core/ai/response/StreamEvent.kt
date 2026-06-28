package com.github.geneing.aichat.core.ai.response

import com.github.geneing.aichat.core.network.dto.ChatMessageDeltaDto
import com.github.geneing.aichat.core.network.dto.ChatStreamChunkDto
import com.github.geneing.aichat.core.network.dto.ToolCallDeltaDto

/**
 * One event emitted by the streaming AI pipeline.
 *
 * - [ContentDelta] is appended to the visible message text.
 * - [ThinkingDelta] is appended to the hidden thinking panel.
 * - [ToolCallDelta] carries one streamed piece of a tool call argument.
 *   When the server closes a tool call, [ToolCallsReady] is emitted with
 *   the complete calls and the runtime should execute them.
 * - [Completed] signals end of stream.
 * - [Failed] signals an error; the consumer should mark the message
 *   as failed and stop.
 */
sealed interface StreamEvent {
    data class ContentDelta(val text: String) : StreamEvent
    data class ThinkingDelta(val text: String) : StreamEvent
    data class ToolCallDelta(val call: ToolCallDeltaDto) : StreamEvent
    data class ToolCallsReady(val calls: List<com.github.geneing.aichat.core.network.dto.ToolCallDto>) : StreamEvent
    data class ToolCallSummary(val toolName: String) : StreamEvent
    data class ToolResult(val toolName: String, val content: String) : StreamEvent
    data object Completed : StreamEvent
    data class Failed(val message: String, val code: Int? = null) : StreamEvent
}

/** Public projection of a parsed chunk for the mapper. */
data class ParsedChunk(
    val role: String?,
    val content: String?,
    val reasoning: String?,
    val toolCalls: List<ToolCallDeltaDto>,
    val finishReason: String?
) {
    fun toEvents(): List<StreamEvent> {
        val events = mutableListOf<StreamEvent>()
        reasoning?.takeIf { it.isNotEmpty() }?.let { events += StreamEvent.ThinkingDelta(it) }
        content?.takeIf { it.isNotEmpty() }?.let { events += StreamEvent.ContentDelta(it) }
        toolCalls.forEach { events += StreamEvent.ToolCallDelta(it) }
        if (finishReason != null) events += StreamEvent.Completed
        return events
    }
}

fun ChatStreamChunkDto.toParsedChunk(): ParsedChunk {
    val choice = choices.firstOrNull()
    val delta: ChatMessageDeltaDto = choice?.delta ?: ChatMessageDeltaDto()
    return ParsedChunk(
        role = delta.role,
        content = delta.content,
        reasoning = delta.reasoning_content,
        toolCalls = delta.toolCalls.orEmpty(),
        finishReason = choice?.finishReason
    )
}
