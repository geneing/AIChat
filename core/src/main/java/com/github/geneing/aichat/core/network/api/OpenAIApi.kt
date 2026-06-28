package com.github.geneing.aichat.core.network.api

import com.github.geneing.aichat.core.network.dto.ChatRequestDto
import com.github.geneing.aichat.core.network.dto.ModelListDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * OpenAI-compatible HTTP API for non-streaming endpoints.
 * Streaming chat/completions is handled by [com.github.geneing.aichat.core.network.AiHttpClient]
 * which uses OkHttp directly to keep the response body as a live Source.
 */
interface OpenAIApi {

    @POST("chat/completions")
    suspend fun chatCompletionsNonStreaming(
        @Header("Authorization") authorization: String? = null,
        @Header("HTTP-Referer") referer: String? = null,
        @Header("X-Title") title: String? = null,
        @Url url: String,
        @Body request: ChatRequestDto
    ): retrofit2.Response<okhttp3.ResponseBody>

    @POST("models")
    suspend fun listModels(@Url url: String): ModelListDto
}
