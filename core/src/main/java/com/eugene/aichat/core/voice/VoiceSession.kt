package com.eugene.aichat.core.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level voice session state, suitable for driving UI.
 */
enum class VoiceSessionState {
    IDLE, LISTENING, THINKING, SPEAKING
}

data class VoiceUiState(
    val state: VoiceSessionState = VoiceSessionState.IDLE,
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val assistantText: String = "",
    val errorMessage: String? = null
)

/**
 * Owns the lifecycle of a single voice conversation turn. The host
 * (Compose screen) wires it to a ChatViewModel so the spoken reply
 * flows through the same streaming pipeline as a typed reply.
 */
@Singleton
class VoiceSession @Inject constructor(
    private val stt: SttEngine,
    private val tts: TtsEngine,
    private val interruptionBus: InterruptionBus
) {

    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    fun startListening() {
        _state.value = _state.value.copy(
            state = VoiceSessionState.LISTENING,
            partialTranscript = "",
            finalTranscript = "",
            errorMessage = null
        )
        stt.start(onEvent = ::onSttEvent)
    }

    fun stopListening() {
        stt.stop()
    }

    fun beginThinking() {
        _state.value = _state.value.copy(state = VoiceSessionState.THINKING)
    }

    fun appendAssistantDelta(delta: String) {
        _state.value = _state.value.copy(
            state = VoiceSessionState.SPEAKING,
            assistantText = _state.value.assistantText + delta
        )
    }

    fun speak(text: String, onDone: () -> Unit) {
        tts.speak(
            text = text,
            utteranceId = "voice-${System.currentTimeMillis()}",
            onStart = { _state.value = _state.value.copy(state = VoiceSessionState.SPEAKING) },
            onDone = {
                _state.value = _state.value.copy(state = VoiceSessionState.IDLE)
                onDone()
            },
            onError = { msg -> _state.value = _state.value.copy(errorMessage = msg) }
        )
    }

    fun stopSpeaking() {
        tts.stop()
        interruptionBus.publish(InterruptionBus.Signal.ManualStop)
        _state.value = _state.value.copy(state = VoiceSessionState.IDLE)
    }

    fun shutdown() {
        stt.cancel()
        tts.shutdown()
    }

    private fun onSttEvent(event: SttEvent) {
        when (event) {
            is SttEvent.Partial -> {
                // Barge-in: if the assistant is currently speaking and we
                // see a partial transcript with content, signal the bus.
                if (_state.value.state == VoiceSessionState.SPEAKING && event.text.isNotBlank()) {
                    interruptionBus.publish(InterruptionBus.Signal.UserBargeIn)
                    tts.stop()
                }
                _state.value = _state.value.copy(partialTranscript = event.text)
            }
            is SttEvent.Final -> {
                _state.value = _state.value.copy(
                    finalTranscript = event.text,
                    partialTranscript = ""
                )
            }
            is SttEvent.Error -> {
                _state.value = _state.value.copy(errorMessage = event.message)
            }
            SttEvent.Ready, SttEvent.Ended -> Unit
        }
    }
}
