package com.eugene.aichat.core.domain.usecase

import com.eugene.aichat.core.ai.client.AiClient
import com.eugene.aichat.core.ai.request.ContextAssembler
import com.eugene.aichat.core.ai.response.StreamEvent
import com.eugene.aichat.core.data.repository.ChatRepository
import com.eugene.aichat.core.domain.model.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

/**
 * Streams a model response for a given chat, accumulating text into the
 * assistant message and emitting incremental [StreamEvent]s.
 *
 * The caller is responsible for cancelling the returned [Flow] to abort
 * the in-flight request (e.g. on user interrupt).
 */
class StreamResponseUseCase @Inject constructor(
    private val aiClient: AiClient,
    private val contextAssembler: ContextAssembler,
    private val chatRepository: ChatRepository
) {
    operator fun invoke(
        chatId: String,
        model: ModelConfig,
        assistantMessageId: String
    ): Flow<StreamEvent> = flow {
        val request = contextAssembler.build(
            chatId = chatId,
            chatRepository = chatRepository,
            model = model
        )
        val started = System.currentTimeMillis()
        var finalText = ""
        var finalThinking = ""
        var errored = false

        aiClient.stream(model, request)
            .onCompletion { err ->
                val latency = System.currentTimeMillis() - started
                if (err == null && !errored) {
                    chatRepository.updateMessageContent(
                        messageId = assistantMessageId,
                        text = finalText.ifBlank { null },
                        streaming = false
                    )
                    if (finalThinking.isNotEmpty()) {
                        chatRepository.updateMessageThinking(
                            messageId = assistantMessageId,
                            text = finalThinking
                        )
                    }
                    chatRepository.markMessageComplete(assistantMessageId, latency)
                }
            }
            .collect { ev ->
                when (ev) {
                    is StreamEvent.ContentDelta -> {
                        finalText += ev.text
                        chatRepository.updateMessageContent(
                            messageId = assistantMessageId,
                            text = finalText,
                            streaming = true
                        )
                    }
                    is StreamEvent.ThinkingDelta -> {
                        finalThinking += ev.text
                        chatRepository.updateMessageThinking(
                            messageId = assistantMessageId,
                            text = finalThinking
                        )
                    }
                    is StreamEvent.Failed -> {
                        errored = true
                        chatRepository.updateMessageContent(
                            messageId = assistantMessageId,
                            text = "⚠️ ${ev.message}",
                            streaming = false
                        )
                        chatRepository.markMessageComplete(assistantMessageId, null)
                    }
                    else -> Unit
                }
                emit(ev)
            }
    }.catch { e ->
        chatRepository.updateMessageContent(
            messageId = assistantMessageId,
            text = "⚠️ ${e.message ?: "Unknown error"}",
            streaming = false
        )
        chatRepository.markMessageComplete(assistantMessageId, null)
        emit(StreamEvent.Failed(e.message ?: "Unknown error"))
    }
}
