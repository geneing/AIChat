package com.eugene.aichat.core.voice

/**
 * Common interface for text-to-speech engines.
 */
interface TtsEngine {
    fun speak(
        text: String,
        utteranceId: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    )
    fun stop()
    fun shutdown()
}
