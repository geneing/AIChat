package com.github.geneing.aichat.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SttEngine] backed by Android's [SpeechRecognizer]. Emits partial
 * results through the callback registered in [start].
 */
@Singleton
class AndroidSpeechRecognizerStt @Inject constructor(
    @ApplicationContext private val context: Context
) : SttEngine {

    private var recognizer: SpeechRecognizer? = null
    private var callback: ((SttEvent) -> Unit)? = null
    private var inSession: Boolean = false

    override val isListening: Boolean get() = inSession

    override fun start(onEvent: (SttEvent) -> Unit, languageTag: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onEvent(SttEvent.Error("Speech recognition not available on this device"))
            return
        }
        stop()
        val rec = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }
        rec.setRecognitionListener(buildListener(onEvent))
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag.ifBlank { Locale.getDefault().toLanguageTag() })
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            rec.startListening(intent)
            inSession = true
            onEvent(SttEvent.Ready)
        } catch (e: Exception) {
            inSession = false
            onEvent(SttEvent.Error(e.message ?: "start failed"))
        }
    }

    override fun stop() {
        recognizer?.let { rec ->
            runCatching { rec.stopListening() }
        }
        inSession = false
    }

    override fun cancel() {
        recognizer?.let { rec ->
            runCatching { rec.cancel() }
            runCatching { rec.destroy() }
        }
        recognizer = null
        inSession = false
    }

    private fun buildListener(onEvent: (SttEvent) -> Unit): RecognitionListener =
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                inSession = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "no match"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "timeout"
                    SpeechRecognizer.ERROR_AUDIO -> "audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "permission denied"
                    else -> "error $error"
                }
                onEvent(SttEvent.Error(message))
            }

            override fun onResults(results: Bundle?) {
                inSession = false
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                onEvent(SttEvent.Final(text))
                onEvent(SttEvent.Ended)
            }

            override fun onPartialResults(partial: Bundle?) {
                val text = partial
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotEmpty()) onEvent(SttEvent.Partial(text))
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
}
