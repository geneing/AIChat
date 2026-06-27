package com.eugene.aichat.core.voice

/**
 * Emits lifecycle events from a speech-to-text engine.
 */
sealed interface SttEvent {
    data class Partial(val text: String) : SttEvent
    data class Final(val text: String) : SttEvent
    data class Error(val message: String) : SttEvent
    data object Ready : SttEvent
    data object Ended : SttEvent
}

/**
 * Common interface for STT engines. Implementations are responsible
 * for managing the underlying recognizer's lifecycle and emitting
 * partial/final transcripts.
 */
interface SttEngine {
    fun start(
        onEvent: (SttEvent) -> Unit,
        languageTag: String = "en-US"
    )
    fun stop()
    fun cancel()
    val isListening: Boolean
}
