package com.github.geneing.aichat.core.ai.tools

import android.content.Intent
import android.net.Uri
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Opens a URL in the system browser. Returns a Text result the model
 * can quote back to the user (typically "Opening …").
 */
class OpenUrlTool : Tool {
    override val name = "open_url"
    override val description = "Open the given URL in the user's default browser."

    override fun parametersSchema(): JsonObject = buildJsonObject {
        put("type", JsonPrimitive("object"))
        putJsonObject("properties") {
            put("url", buildJsonObject {
                put("type", JsonPrimitive("string"))
                put("description", JsonPrimitive("Absolute http(s) URL to open."))
            })
        }
        put("required", kotlinx.serialization.json.buildJsonArray { add(JsonPrimitive("url")) })
    }

    override suspend fun execute(arguments: JsonElement, ctx: ToolContext): ToolResult = runCatching {
        val url = arguments.asObjectString("url")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.caller.startActivity(intent)
        ToolResult.Text("Opened $url")
    }.getOrElse { ToolResult.Error(it.message ?: "open_url failed") }

    private fun JsonElement.asObjectString(key: String): String =
        (this as JsonObject)[key]?.jsonPrimitive?.content
            ?: error("$key is required for open_url")
}

private fun kotlinx.serialization.json.JsonObjectBuilder.putJsonObject(
    name: String,
    build: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit
) {
    put(name, buildJsonObject(build))
}
