package com.eugene.aichat.core.ai.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.eugene.aichat.core.data.db.dao.PendingActionDao
import com.eugene.aichat.core.data.db.entity.PendingActionEntity
import com.eugene.aichat.core.network.dto.ToolCallDto
import com.eugene.aichat.core.util.UrlParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists every tool call and every "look like a URL" snippet in the
 * final assistant text to the [PendingActionDao] so the UI can offer
 * a one-tap launch. Pure data: this class doesn't start activities.
 * The UI is responsible for asking the user to confirm before
 * dispatching intents, since some of these open external apps.
 */
@Singleton
class ActionDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: PendingActionDao
) {
    suspend fun recordToolCall(messageId: String, call: ToolCallDto, result: ToolResult) {
        val kind = when (call.function.name) {
            "open_url" -> Kind.OPEN_URL
            "open_maps" -> Kind.OPEN_MAPS
            "web_search" -> Kind.WEB_SEARCH
            "get_location" -> Kind.GET_LOCATION
            "get_local_info" -> Kind.LOCAL_INFO
            "share_text" -> Kind.SHARE
            else -> Kind.OTHER
        }
        dao.upsert(
            PendingActionEntity(
                id = call.id.ifBlank { java.util.UUID.randomUUID().toString() },
                messageId = messageId,
                kind = kind,
                payloadJson = buildJsonObject {
                    put("name", JsonPrimitive(call.function.name))
                    put("arguments", JsonPrimitive(call.function.arguments))
                    put("result", JsonPrimitive(result.content))
                }.toString(),
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Scans the final assistant text for plain URLs and geo: URIs and
     * records each as a PendingAction. Runs after a successful stream
     * so the user can tap any citation in the reply.
     */
    suspend fun scanFinalText(messageId: String, text: String) {
        if (text.isBlank()) return
        val now = System.currentTimeMillis()
        var idx = 0
        for (url in UrlParser.findAll(text)) {
            val kind = if (url.startsWith("geo:")) Kind.OPEN_MAPS else Kind.OPEN_URL
            dao.upsert(
                PendingActionEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    messageId = messageId,
                    kind = kind,
                    payloadJson = buildJsonObject {
                        put("url", JsonPrimitive(url))
                    }.toString(),
                    status = "PENDING",
                    createdAt = now + idx++
                )
            )
        }
    }

    suspend fun markDispatched(id: String) {
        dao.updateStatus(id, "DISPATCHED")
    }

    /** Build an intent the UI can launch (open a URL in the browser, etc.). */
    fun intentFor(payload: String): Intent? {
        val obj = runCatching {
            kotlinx.serialization.json.Json.parseToJsonElement(payload) as kotlinx.serialization.json.JsonObject
        }.getOrNull() ?: return null
        val url = (obj["url"] as? JsonPrimitive)?.content ?: return null
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    object Kind {
        const val OPEN_URL = "OPEN_URL"
        const val OPEN_MAPS = "OPEN_MAPS"
        const val WEB_SEARCH = "WEB_SEARCH"
        const val GET_LOCATION = "GET_LOCATION"
        const val LOCAL_INFO = "LOCAL_INFO"
        const val SHARE = "SHARE"
        const val OTHER = "OTHER"
    }
}
