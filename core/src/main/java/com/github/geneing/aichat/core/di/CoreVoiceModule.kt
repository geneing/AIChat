package com.github.geneing.aichat.core.di

import com.github.geneing.aichat.core.voice.AndroidSpeechRecognizerStt
import com.github.geneing.aichat.core.voice.AndroidTtsEngine
import com.github.geneing.aichat.core.voice.SttEngine
import com.github.geneing.aichat.core.voice.TtsEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreVoiceModule {

    @Binds
    @Singleton
    abstract fun bindSttEngine(impl: AndroidSpeechRecognizerStt): SttEngine

    @Binds
    @Singleton
    abstract fun bindTtsEngine(impl: AndroidTtsEngine): TtsEngine
}
