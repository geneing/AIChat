package com.github.geneing.aichat.core.ai.tools

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetLocationTool : Tool {
    override val name = "get_location"
    override val description =
        "Return the user's current approximate coordinates and accuracy, if available."

    override fun parametersSchema(): JsonObject = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put("properties", buildJsonObject { })
    }

    override suspend fun execute(arguments: JsonElement, ctx: ToolContext): ToolResult {
        val loc = ctx.locationProvider.lastKnown()
            ?: return ToolResult.Error("No location available; permission may be missing.")
        val text = "${loc.latitude},${loc.longitude} (±${loc.accuracy.toInt()}m, provider=${loc.provider})"
        return ToolResult.Text(text)
    }
}
