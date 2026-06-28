package com.github.geneing.aichat.core.ai.provider

import com.github.geneing.aichat.core.domain.model.ProviderType

/**
 * ProviderAdapter describes the HTTP surface of an AI provider.
 * The three supported providers all expose an OpenAI-compatible
 * chat/completions endpoint, so the differences are limited to the
 * base URL and a couple of attribution headers.
 */
interface ProviderAdapter {
    val type: ProviderType
    val baseUrl: String
    val completionsPath: String
    val modelsPath: String
    /** Extra headers required for this provider (e.g. attribution for OpenRouter). */
    fun extraHeaders(): Map<String, String> = emptyMap()

    /** Default model id to suggest when adding this provider. */
    fun defaultModel(): String
}

class OpenAIProvider : ProviderAdapter {
    override val type = ProviderType.OPENAI
    override val baseUrl = "https://api.openai.com/v1"
    override val completionsPath = "/chat/completions"
    override val modelsPath = "/models"
    override fun defaultModel() = "gpt-4o-mini"
}

class OpenRouterProvider : ProviderAdapter {
    override val type = ProviderType.OPENROUTER
    override val baseUrl = "https://openrouter.ai/api/v1"
    override val completionsPath = "/chat/completions"
    override val modelsPath = "/models"
    override fun extraHeaders(): Map<String, String> = mapOf(
        "HTTP-Referer" to "https://aichat.local",
        "X-Title" to "AIChat"
    )
    override fun defaultModel() = "openrouter/auto"
}

class OpenCodeProvider : ProviderAdapter {
    override val type = ProviderType.OPENCODE
    override val baseUrl = "https://opencode.ai/api/v1"
    override val completionsPath = "/chat/completions"
    override val modelsPath = "/models"
    override fun defaultModel() = "opencode-ai/default"
}

class ProviderRegistry(
    private val adapters: Map<ProviderType, ProviderAdapter>
) {
    fun get(type: ProviderType): ProviderAdapter =
        adapters[type] ?: error("No provider adapter for $type")
}
