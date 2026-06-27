package com.eugene.aichat.core.ai.tools

import com.eugene.aichat.core.ai.location.LocationProvider
import com.eugene.aichat.core.network.dto.ToolDefinitionDto
import com.eugene.aichat.core.network.dto.ToolFunctionDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A single callable tool. The model may invoke it through the OpenAI
 * tool-calling protocol; on a ToolCallsReady event the agent runtime
 * iterates the calls, parses the JSON arguments, and dispatches.
 */
interface Tool {
    val name: String
    val description: String
    fun parametersSchema(): JsonObject

    suspend fun execute(arguments: JsonElement, ctx: ToolContext): ToolResult

    fun toDefinition(): ToolDefinitionDto = ToolDefinitionDto(
        function = ToolFunctionDto(
            name = name,
            description = description,
            parameters = parametersSchema()
        )
    )
}

/**
 * Ambient context passed to every tool invocation. Holds references
 * that the tool might need (Activity, ChatRepository, etc.).
 */
interface ToolContext {
    val caller: android.content.Context
    val chatId: String?
    val locationProvider: LocationProvider
    val webSearchProvider: WebSearchProvider
}

/**
 * Result of a tool execution, returned to the agent runtime which
 * packages it as a tool message in the next chat-completions request.
 */
sealed interface ToolResult {
    val content: String
    data class Text(override val content: String) : ToolResult
    data class Error(override val content: String) : ToolResult
}
