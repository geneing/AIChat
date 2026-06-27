package com.eugene.aichat.core.voice

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for voice-mode interruption signals. The
 * STT engine publishes [UserBargeIn] when the user starts speaking
 * while the assistant is still speaking; the AI client subscribes and
 * cancels the in-flight stream + TTS.
 */
@Singleton
class InterruptionBus @Inject constructor() {
    sealed interface Signal {
        data object UserBargeIn : Signal
        data object ManualStop : Signal
    }

    private val _signals = MutableSharedFlow<Signal>(extraBufferCapacity = 4)
    val signals: SharedFlow<Signal> = _signals.asSharedFlow()

    fun publish(signal: Signal) {
        _signals.tryEmit(signal)
    }
}
