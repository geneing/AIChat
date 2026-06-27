package com.eugene.aichat.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TtsEngine] backed by Android's [TextToSpeech].
 */
@Singleton
class AndroidTtsEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : TtsEngine {

    private var tts: TextToSpeech? = null
    private var ready: Boolean = false
    private var pendingInit: ((Boolean) -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            pendingInit?.invoke(ready)
            pendingInit = null
        }
    }

    override fun speak(
        text: String,
        utteranceId: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val engine = tts ?: return
        if (!ready) {
            pendingInit = { ok ->
                if (ok) speak(text, utteranceId, onStart, onDone, onError)
                else onError("tts init failed")
            }
            return
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { onStart() }
            override fun onDone(utteranceId: String?) { onDone() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onError("tts error") }
        })
        engine.language = Locale.getDefault()
        engine.setSpeechRate(1.0f)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
