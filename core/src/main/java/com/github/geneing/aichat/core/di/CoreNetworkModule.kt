package com.github.geneing.aichat.core.di

import com.github.geneing.aichat.core.ai.client.AiClient
import com.github.geneing.aichat.core.ai.client.AiClientImpl
import com.github.geneing.aichat.core.ai.location.LocationProvider
import com.github.geneing.aichat.core.ai.provider.OpenAIProvider
import com.github.geneing.aichat.core.ai.provider.OpenCodeProvider
import com.github.geneing.aichat.core.ai.provider.OpenRouterProvider
import com.github.geneing.aichat.core.ai.provider.ProviderAdapter
import com.github.geneing.aichat.core.ai.provider.ProviderRegistry
import com.github.geneing.aichat.core.ai.response.SseParser
import com.github.geneing.aichat.core.ai.tools.NoopWebSearchProvider
import com.github.geneing.aichat.core.ai.tools.WebSearchProvider
import com.github.geneing.aichat.core.domain.model.ProviderType
import com.github.geneing.aichat.core.network.api.OpenAIApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.github.geneing.aichat.core.data.serialization.AppJson
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreNetworkProviders {

    @Provides
    @Singleton
    fun provideUserAgentInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .header("User-Agent", "AIChat/0.1 (Android)")
            .build()
        chain.proceed(req)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(userAgentInterceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // streaming
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://placeholder.local/")
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideOpenAIApi(retrofit: Retrofit): OpenAIApi =
        retrofit.create(OpenAIApi::class.java)

    @Provides
    @Singleton
    fun provideProviderRegistry(): ProviderRegistry {
        val adapters: Map<ProviderType, ProviderAdapter> = mapOf(
            ProviderType.OPENAI to OpenAIProvider(),
            ProviderType.OPENROUTER to OpenRouterProvider(),
            ProviderType.OPENCODE to OpenCodeProvider()
        )
        return ProviderRegistry(adapters)
    }

    @Provides
    @Singleton
    fun provideSseParser(): SseParser = SseParser()

    @Provides
    @Singleton
    fun provideWebSearchProvider(): WebSearchProvider = NoopWebSearchProvider()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreNetworkBindings {

    @Binds
    @Singleton
    abstract fun bindAiClient(impl: AiClientImpl): AiClient
}
