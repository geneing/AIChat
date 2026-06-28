package com.github.geneing.aichat.core.ai.provider

import com.github.geneing.aichat.core.domain.model.ProviderType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ProviderRegistryTest {

    @Test
    fun `registry returns correct adapter for each type`() {
        val registry = ProviderRegistry(
            mapOf(
                ProviderType.OPENAI to OpenAIProvider(),
                ProviderType.OPENROUTER to OpenRouterProvider(),
                ProviderType.OPENCODE to OpenCodeProvider()
            )
        )
        assertThat(registry.get(ProviderType.OPENAI).baseUrl).startsWith("https://api.openai.com")
        assertThat(registry.get(ProviderType.OPENROUTER).baseUrl).startsWith("https://openrouter.ai")
        assertThat(registry.get(ProviderType.OPENCODE).baseUrl).startsWith("https://opencode.ai")
    }

    @Test
    fun `openrouter adapter requires attribution headers`() {
        val adapter = OpenRouterProvider()
        val headers = adapter.extraHeaders()
        assertThat(headers).containsKey("HTTP-Referer")
        assertThat(headers).containsKey("X-Title")
    }

    @Test
    fun `openai adapter does not require extra headers`() {
        val adapter = OpenAIProvider()
        assertThat(adapter.extraHeaders()).isEmpty()
    }
}
