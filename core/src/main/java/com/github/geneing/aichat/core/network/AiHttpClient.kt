package com.github.geneing.aichat.core.network

import com.github.geneing.aichat.core.data.serialization.AppJson
import com.github.geneing.aichat.core.network.dto.ChatRequestDto
import com.github.geneing.aichat.core.network.dto.ErrorBodyDto
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Low-level HTTP client for the AI providers. Wraps OkHttp so the streaming
 * body is exposed as a [Flow] of raw SSE lines, leaving the parsing to
 * `SseParser`.
 */
@Singleton
class AiHttpClient @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun execute(
        baseUrl: String,
        path: String,
        apiKey: String?,
        extraHeaders: Map<String, String> = emptyMap(),
        request: ChatRequestDto
    ): ResponseBody = suspendCancellableCoroutine { cont ->
        val url = joinUrl(baseUrl, path)
        val payload = AppJson.encodeToString(ChatRequestDto.serializer(), request)
            .toRequestBody(JSON_MEDIA_TYPE)

        val builder = Request.Builder()
            .url(url)
            .post(payload)
            .header("Accept", "text/event-stream")
            .header("Content-Type", "application/json")

        if (!apiKey.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }
        for ((k, v) in extraHeaders) builder.header(k, v)

        val call = client.newCall(builder.build())
        cont.invokeOnCancellation { runCatching { call.cancel() } }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string().orEmpty()
                    val err = runCatching {
                        AppJson.decodeFromString(ErrorBodyDto.serializer(), errorBody)
                    }.getOrNull()
                    val msg = err?.error?.message
                        ?: err?.message
                        ?: "HTTP ${response.code}: ${response.message}"
                    response.close()
                    if (cont.isActive) cont.resumeWithException(AiHttpException(response.code, msg))
                    return
                }
                val body = response.body
                if (body == null) {
                    response.close()
                    if (cont.isActive) cont.resumeWithException(AiHttpException(response.code, "Empty body"))
                    return
                }
                if (cont.isActive) cont.resume(body)
            }
        })
    }

    fun streamSse(
        baseUrl: String,
        path: String,
        apiKey: String?,
        extraHeaders: Map<String, String> = emptyMap(),
        request: ChatRequestDto
    ): Flow<String> = callbackFlow {
        val url = joinUrl(baseUrl, path)
        val payload = AppJson.encodeToString(ChatRequestDto.serializer(), request)
            .toRequestBody(JSON_MEDIA_TYPE)

        val builder = Request.Builder()
            .url(url)
            .post(payload)
            .header("Accept", "text/event-stream")
            .header("Content-Type", "application/json")

        if (!apiKey.isNullOrBlank()) builder.header("Authorization", "Bearer $apiKey")
        for ((k, v) in extraHeaders) builder.header(k, v)

        val call = client.newCall(builder.build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val msg = "HTTP ${response.code}: ${response.message}"
                    response.close()
                    close(AiHttpException(response.code, msg))
                    return
                }
                val body = response.body
                if (body == null) {
                    response.close()
                    close(AiHttpException(response.code, "Empty body"))
                    return
                }
                val source: BufferedSource = body.source()
                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        trySend(line)
                    }
                    body.close()
                    close()
                } catch (e: Exception) {
                    body.close()
                    close(e)
                }
            }
        })
        awaitClose { runCatching { call.cancel() } }
    }

    private fun joinUrl(base: String, path: String): String {
        val b = base.trimEnd('/')
        val p = if (path.startsWith("/")) path else "/$path"
        return "$b$p"
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

class AiHttpException(val code: Int, message: String) : RuntimeException(message)
