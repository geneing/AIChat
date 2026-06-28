package com.github.geneing.aichat.core.domain.usecase

import com.github.geneing.aichat.core.domain.model.ModelConfig
import javax.inject.Inject

/**
 * Generates a short chat title from the first user/assistant exchange.
 *
 * Currently a no-op stub: it returns `null`, which leaves the chat with
 * the default "New chat" title. A future implementation will call a
 * lightweight model with a "summarize into <= 50 chars" prompt and
 * post-process the result (strip quotes, truncate, trim punctuation).
 */
class GenerateTitleUseCase @Inject constructor() {
    suspend operator fun invoke(chatId: String, model: ModelConfig): String? = null
}
