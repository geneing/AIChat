package com.github.geneing.aichat.core.domain.usecase

import com.github.geneing.aichat.core.data.repository.ChatRepository
import javax.inject.Inject

/**
 * Marks an in-flight assistant message as complete (no further streaming).
 * Used when the user interrupts (sends a new message, hits stop, or starts
 * voice mode with barge-in). Cancellation of the actual network stream is
 * handled by the coroutine scope holding the [kotlinx.coroutines.Job].
 */
class CancelStreamUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(messageId: String) {
        chatRepository.markMessageComplete(messageId, latencyMs = null)
    }
}
