package com.eugene.aichat.core.ai.tools

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Lightweight interface for a web search backend. The default
 * implementation is a no-op (returns an explanatory message); the
 * SerpAPI / Brave / Google CSE wiring is in settings.
 */
interface WebSearchProvider {
    suspend fun search(query: String, topK: Int = 5): List<WebSearchResult>
    val enabled: Boolean
}

data class WebSearchResult(
    val title: String,
    val url: String,
    val snippet: String
)

class WebSearchTool : Tool {
    override val name = "web_search"
    override val description = "Search the web for up-to-date information."

    override fun parametersSchema(): JsonObject = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put("properties", buildJsonObject {
            put("query", buildJsonObject {
                put("type", JsonPrimitive("string"))
                put("description", JsonPrimitive("Search query."))
            })
        })
        put("required", buildJsonArray { add(JsonPrimitive("query")) })
    }

    override suspend fun execute(arguments: JsonElement, ctx: ToolContext): ToolResult {
        val query = (arguments as JsonObject)["query"]?.let { (it as JsonPrimitive).content }
            ?: return ToolResult.Error("query is required")
        if (!ctx.webSearchProvider.enabled) {
            return ToolResult.Text(
                "Web search is not configured. Add a SerpAPI or Brave Search key in Settings to enable web_search."
            )
        }
        val results = ctx.webSearchProvider.search(query)
        if (results.isEmpty()) return ToolResult.Text("No results found for '$query'.")
        val body = results.joinToString("\n\n") {
            "${it.title}\n${it.url}\n${it.snippet}"
        }
        return ToolResult.Text(body)
    }
}

class GetLocalInfoTool : Tool {
    override val name = "get_local_info"
    override val description = "Return local context: current time, locale, and time zone."

    override fun parametersSchema(): JsonObject = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put("properties", buildJsonObject {})
    }

    override suspend fun execute(arguments: JsonElement, ctx: ToolContext): ToolResult {
        val zone = ZoneId.systemDefault()
        val time = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            .withZone(zone)
            .format(Instant.now())
        val text = "Time: $time\nLocale: ${Locale.getDefault()}\nTimeZone: $zone"
        return ToolResult.Text(text)
    }
}
