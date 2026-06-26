package com.eugene.aichat.core.ai.client

import com.eugene.aichat.core.ai.response.StreamEvent
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.network.dto.ChatRequestDto
import kotlinx.coroutines.flow.Flow

/**
 * Streaming chat-completions client. Implementations are responsible
 * for mapping the [ModelConfig] to a provider request and emitting a
 * [Flow] of [StreamEvent]s as the model responds.
 */
interface AiClient {
    fun stream(model: ModelConfig, request: ChatRequestDto): Flow<StreamEvent>
}
