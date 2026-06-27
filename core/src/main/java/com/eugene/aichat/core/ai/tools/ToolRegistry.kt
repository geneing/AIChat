package com.eugene.aichat.core.ai.tools

import com.eugene.aichat.core.ai.location.LocationProvider
import com.eugene.aichat.core.network.dto.ToolCallDto
import com.eugene.aichat.core.network.dto.ToolDefinitionDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the set of [Tool]s available to the model and dispatches a
 * [ToolCallDto] to the right tool, parsing its arguments.
 */
@Singleton
class ToolRegistry @Inject constructor(
    val locationProvider: LocationProvider,
    val webSearchProvider: WebSearchProvider
) {
    private val tools: Map<String, Tool> = listOf(
        OpenUrlTool(),
        OpenMapsTool(),
        WebSearchTool(),
        GetLocationTool(),
        GetLocalInfoTool()
    ).associateBy { it.name }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun definitions(): List<ToolDefinitionDto> = tools.values.map { it.toDefinition() }

    fun names(): Set<String> = tools.keys

    fun has(name: String): Boolean = tools.containsKey(name)

    suspend fun execute(call: ToolCallDto, ctx: ToolContext): ToolResult {
        val tool = tools[call.function.name]
            ?: return ToolResult.Error("Unknown tool: ${call.function.name}")
        val args = runCatching { json.parseToJsonElement(call.function.arguments) }
            .getOrElse { return ToolResult.Error("Bad arguments: ${it.message}") }
        return runCatching { tool.execute(args, ctx) }
            .getOrElse { ToolResult.Error(it.message ?: "tool ${call.function.name} failed") }
    }
}
