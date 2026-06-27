package com.eugene.aichat.core.ai.agents

import app.cash.turbine.test
import com.eugene.aichat.core.ai.response.StreamEvent
import com.eugene.aichat.core.ai.tools.ActionDispatcher
import com.eugene.aichat.core.ai.tools.ToolContext
import com.eugene.aichat.core.ai.tools.ToolRegistry
import com.eugene.aichat.core.ai.tools.ToolResult
import com.eugene.aichat.core.data.db.dao.PendingActionDao
import com.eugene.aichat.core.data.db.entity.PendingActionEntity
import com.eugene.aichat.core.domain.model.Agent
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.domain.model.ProviderType
import com.eugene.aichat.core.network.dto.ChatRequestDto
import com.eugene.aichat.core.network.dto.ToolCallDto
import com.eugene.aichat.core.network.dto.ToolCallFunctionDto
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import android.content.Context
import com.eugene.aichat.core.ai.location.LocationProvider
import com.eugene.aichat.core.ai.tools.WebSearchProvider

class AgentRuntimeTest {

    private fun newModel() = ModelConfig(
        id = "m1", displayName = "gpt-4o", providerType = ProviderType.OPENAI,
        baseUrl = "https://api.openai.com/v1", model = "gpt-4o", apiKey = "sk-x",
        temperature = 0.5f, topP = 1f, maxTokens = 1024,
        supportsTools = true, supportsVision = false, supportsAudio = false, isDefault = false
    )

    private fun newAgent(maxSteps: Int = 3) = Agent(
        id = "a1", name = "tester", description = "d", systemPrompt = "sp",
        modelConfigHint = null, modelConfigId = null,
        skillIds = emptyList(), toolAllowList = listOf("web_search"),
        maxSteps = maxSteps, temperature = null,
        isBuiltIn = true, isEnabled = true, version = 1
    )

    @Test
    fun `final answer emitted when no tool calls`() = runTest {
        val location = mockk<LocationProvider>(relaxed = true)
        val web = mockk<WebSearchProvider>(relaxed = true)
        val ctx = TestToolContext(location, web)
        val registry = ToolRegistry(location, web)
        val dao = mockk<PendingActionDao>(relaxed = true)
        coEvery { dao.upsert(any()) } returns Unit
        val dispatcher = ActionDispatcher(mockk(relaxed = true), dao)
        val runtime = AgentRuntime(registry, dispatcher)

        runtime.run(
            agent = newAgent(),
            model = newModel(),
            initialMessages = emptyList(),
            toolContext = ctx,
            messageId = "m1",
            modelStream = { _ -> flowOf(StreamEvent.ContentDelta("hi"), StreamEvent.Completed) }
        ).test {
            val events = mutableListOf<Step>()
            while (true) {
                val ev = awaitItem()
                events += ev
                if (ev is Step.FinalAnswer) break
            }
            cancelAndIgnoreRemainingEvents()
            assertThat(events.filterIsInstance<Step.Thinking>()).hasSize(1)
            assertThat(events.last()).isEqualTo(Step.FinalAnswer("hi"))
        }
    }

    @Test
    fun `tool call then final answer`() = runTest {
        val location = mockk<LocationProvider>(relaxed = true)
        val web = mockk<WebSearchProvider>(relaxed = true)
        val ctx = TestToolContext(location, web)
        val registry = ToolRegistry(location, web)
        val dao = mockk<PendingActionDao>(relaxed = true)
        coEvery { dao.upsert(any()) } returns Unit
        val dispatcher = ActionDispatcher(mockk(relaxed = true), dao)
        val runtime = AgentRuntime(registry, dispatcher)

        val call = ToolCallDto(
            id = "call_1",
            function = ToolCallFunctionDto(
                name = "web_search",
                arguments = """{"query":"kotlin"}"""
            )
        )

        val second = flowOf<StreamEvent>(
            StreamEvent.ContentDelta("search says yes"),
            StreamEvent.Completed
        )
        var firstInvocation = true
        runtime.run(
            agent = newAgent(),
            model = newModel(),
            initialMessages = emptyList(),
            toolContext = ctx,
            messageId = "m1",
            modelStream = { _ ->
                if (firstInvocation) {
                    firstInvocation = false
                    flow {
                        emit(StreamEvent.ToolCallsReady(listOf(call)))
                        emit(StreamEvent.Completed)
                    }
                } else second
            }
        ).test {
            val events = mutableListOf<Step>()
            while (true) {
                val ev = awaitItem()
                events += ev
                if (ev is Step.FinalAnswer) break
            }
            cancelAndIgnoreRemainingEvents()
            val toolSteps = events.filterIsInstance<Step.ToolInvocation>()
            assertThat(toolSteps).hasSize(1)
            assertThat(toolSteps[0].name).isEqualTo("web_search")
            assertThat(events.last()).isEqualTo(Step.FinalAnswer("search says yes"))
        }
    }

    @Test
    fun `failed stream produces Failed step`() = runTest {
        val location = mockk<LocationProvider>(relaxed = true)
        val web = mockk<WebSearchProvider>(relaxed = true)
        val ctx = TestToolContext(location, web)
        val registry = ToolRegistry(location, web)
        val dao = mockk<PendingActionDao>(relaxed = true)
        coEvery { dao.upsert(any()) } returns Unit
        val dispatcher = ActionDispatcher(mockk(relaxed = true), dao)
        val runtime = AgentRuntime(registry, dispatcher)

        runtime.run(
            agent = newAgent(),
            model = newModel(),
            initialMessages = emptyList(),
            toolContext = ctx,
            messageId = "m1",
            modelStream = { _ -> flowOf(StreamEvent.Failed("boom")) }
        ).test {
            val events = mutableListOf<Step>()
            while (true) {
                val ev = awaitItem()
                events += ev
                if (ev is Step.Failed) break
            }
            cancelAndIgnoreRemainingEvents()
            assertThat(events.filterIsInstance<Step.Failed>()).hasSize(1)
            assertThat(events.filterIsInstance<Step.Failed>().first().message).isEqualTo("boom")
        }
    }
}

private class TestToolContext(
    override val locationProvider: LocationProvider,
    override val webSearchProvider: WebSearchProvider
) : ToolContext {
    override val caller: Context = mockk(relaxed = true)
    override val chatId: String? = "c1"
}
