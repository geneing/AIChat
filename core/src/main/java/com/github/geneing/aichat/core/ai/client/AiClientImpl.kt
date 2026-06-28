package com.github.geneing.aichat.core.ai.client

import com.github.geneing.aichat.core.ai.provider.ProviderRegistry
import com.github.geneing.aichat.core.ai.response.SseParser
import com.github.geneing.aichat.core.ai.response.StreamEvent
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.ProviderType
import com.github.geneing.aichat.core.network.AiHttpClient
import com.github.geneing.aichat.core.network.AiHttpException
import com.github.geneing.aichat.core.network.dto.ChatRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiClientImpl @Inject constructor(
    private val http: AiHttpClient,
    private val registry: ProviderRegistry,
    private val sseParser: SseParser
) : AiClient {

    override fun stream(model: ModelConfig, request: ChatRequestDto): Flow<StreamEvent> = flow {
        val provider = registry.get(ProviderType.valueOf(model.providerType.name))
        val lines = http.streamSse(
            baseUrl = model.baseUrl.ifBlank { provider.baseUrl },
            path = provider.completionsPath,
            apiKey = model.apiKey,
            extraHeaders = provider.extraHeaders(),
            request = request
        )
        sseParser.parse(lines).collect { emit(it) }
    }.catch { e ->
        when (e) {
            is AiHttpException -> emit(StreamEvent.Failed(e.message ?: "HTTP ${e.code}", e.code))
            else -> emit(StreamEvent.Failed(e.message ?: "Network error"))
        }
    }
}
