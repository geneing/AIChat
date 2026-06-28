package com.github.geneing.aichat.core.ai.tools

import android.content.Intent
import android.net.Uri
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Launches the user's preferred maps app with the given destination.
 * Accepts either a "geo:lat,lng" URI, a free-form "q" address, or a
 * Google Maps directions URL.
 */
class OpenMapsTool : Tool {
    override val name = "open_maps"
    override val description = "Open a location in the user's preferred maps app."

    override fun parametersSchema(): JsonObject = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put("properties", buildJsonObject {
            put("query", buildJsonObject {
                put("type", JsonPrimitive("string"))
                put("description", JsonPrimitive("Free-form address or place name."))
            })
            put("lat", buildJsonObject {
                put("type", JsonPrimitive("number"))
                put("description", JsonPrimitive("Latitude (decimal)."))
            })
            put("lng", buildJsonObject {
                put("type", JsonPrimitive("number"))
                put("description", JsonPrimitive("Longitude (decimal)."))
            })
        })
    }

    override suspend fun execute(arguments: JsonElement, ctx: ToolContext): ToolResult = runCatching {
        val obj = arguments as JsonObject
        val uri: Uri = when {
            obj.containsKey("lat") && obj.containsKey("lng") -> {
                val lat = obj["lat"]!!.jsonPrimitive.content
                val lng = obj["lng"]!!.jsonPrimitive.content
                Uri.parse("geo:$lat,$lng?q=$lat,$lng")
            }
            obj.containsKey("query") -> {
                val q = obj["query"]!!.jsonPrimitive.content
                Uri.parse("geo:0,0?q=" + Uri.encode(q))
            }
            else -> error("open_maps requires either query or lat+lng")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.caller.startActivity(intent)
        ToolResult.Text("Opening maps: $uri")
    }.getOrElse { ToolResult.Error(it.message ?: "open_maps failed") }
}
