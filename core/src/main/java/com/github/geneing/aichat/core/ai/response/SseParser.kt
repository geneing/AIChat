package com.github.geneing.aichat.core.ai.response

import com.github.geneing.aichat.core.data.serialization.AppJson
import com.github.geneing.aichat.core.network.dto.ChatStreamChunkDto
import com.github.geneing.aichat.core.network.dto.ToolCallDto
import com.github.geneing.aichat.core.network.dto.ToolCallFunctionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform

private const val DONE_SENTINEL = "[DONE]"

/**
 * Parses Server-Sent Events from a line Flow into [StreamEvent]s.
 *
 * Each SSE block has the form:
 *   data: {"id":"…","choices":[…]}
 *   data: {"id":"…","choices":[…]}
 *   data: [DONE]
 *   (blank line separator)
 *
 * Blank lines and other event fields are ignored. [DONE] terminates the
 * stream. If a `data:` line is not valid JSON, it is skipped silently
 * (some providers send keep-alive comments).
 */
class SseParser {

    fun parse(lines: Flow<String>): Flow<StreamEvent> = flow {
        val toolCalls = mutableMapOf<Int, ToolCallBuilder>()
        lines.collect { raw ->
            val line = raw.trimEnd()
            if (line.isEmpty()) return@collect
            if (!line.startsWith(DATA_PREFIX)) return@collect
            val data = line.removePrefix(DATA_PREFIX).trim()
            if (data == DONE_SENTINEL) {
                if (toolCalls.isNotEmpty()) {
                    emit(StreamEvent.ToolCallsReady(toolCalls.values.map { it.build() }))
                    toolCalls.clear()
                }
                emit(StreamEvent.Completed)
                return@collect
            }
            val chunk = runCatching {
                AppJson.decodeFromString(ChatStreamChunkDto.serializer(), data)
            }.getOrNull() ?: return@collect

            val parsed = chunk.toParsedChunk()
            for (call in parsed.toolCalls) {
                val builder = toolCalls.getOrPut(call.index) { ToolCallBuilder() }
                builder.merge(call)
            }
            parsed.toEvents().forEach { emit(it) }
        }
    }
}

private const val DATA_PREFIX = "data:"

private class ToolCallBuilder {
    var id: String? = null
    var type: String = "function"
    var name: String? = null
    val args = StringBuilder()

    fun merge(delta: com.github.geneing.aichat.core.network.dto.ToolCallDeltaDto) {
        delta.id?.let { id = it }
        delta.type?.let { type = it }
        delta.function?.name?.let { name = it }
        delta.function?.arguments?.let { args.append(it) }
    }

    fun build(): ToolCallDto = ToolCallDto(
        id = id ?: "",
        type = type,
        function = ToolCallFunctionDto(
            name = name.orEmpty(),
            arguments = args.toString()
        )
    )
}
