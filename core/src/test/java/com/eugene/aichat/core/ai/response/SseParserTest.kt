package com.eugene.aichat.core.ai.response

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SseParserTest {

    @Test
    fun `parses content deltas and emits Completed on DONE`() = runTest {
        val sse = SseParser()
        val lines = flow {
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"Hel\"}}]}")
            emit("")
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"lo\"}}]}")
            emit("data: [DONE]")
        }
        sse.parse(lines).test {
            val evs = mutableListOf<StreamEvent>()
            while (true) {
                val item = awaitItem()
                evs += item
                if (item is StreamEvent.Completed) break
            }
            cancelAndIgnoreRemainingEvents()
            val content = evs.filterIsInstance<StreamEvent.ContentDelta>().map { it.text }
            assertThat(content).containsExactly("Hel", "lo").inOrder()
            assertThat(evs.last()).isEqualTo(StreamEvent.Completed)
        }
    }

    @Test
    fun `parses reasoning_content into ThinkingDelta`() = runTest {
        val sse = SseParser()
        val lines = flow {
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"reasoning_content\":\"Let me think...\"}}]}")
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Final answer\"}}]}")
            emit("data: [DONE]")
        }
        sse.parse(lines).test {
            val first = awaitItem()
            assertThat(first).isInstanceOf(StreamEvent.ThinkingDelta::class.java)
            assertThat((first as StreamEvent.ThinkingDelta).text).isEqualTo("Let me think...")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `assembles streamed tool call deltas into one ToolCallsReady`() = runTest {
        val sse = SseParser()
        val lines = flow {
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"call_1\",\"type\":\"function\",\"function\":{\"name\":\"open_url\",\"arguments\":\"{\\\"url\\\":\"}}]}}]}")
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"function\":{\"arguments\":\"\\\"https://example.com\\\"}\"}}]}}]}")
            emit("data: [DONE]")
        }
        sse.parse(lines).test {
            val collected = mutableListOf<StreamEvent>()
            while (true) {
                val item = awaitItem()
                collected += item
                if (item is StreamEvent.ToolCallsReady) break
            }
            cancelAndIgnoreRemainingEvents()
            val ready = collected.filterIsInstance<StreamEvent.ToolCallsReady>().single()
            assertThat(ready.calls).hasSize(1)
            val call = ready.calls.single()
            assertThat(call.id).isEqualTo("call_1")
            assertThat(call.function.name).isEqualTo("open_url")
            assertThat(call.function.arguments).isEqualTo("{\"url\":\"https://example.com\"}")
        }
    }

    @Test
    fun `skips blank and non-data lines`() = runTest {
        val sse = SseParser()
        val lines = flow {
            emit(": keep-alive")
            emit("event: ping")
            emit("")
            emit("data: {\"id\":\"a\",\"model\":\"m\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"x\"}}]}")
            emit("data: [DONE]")
        }
        sse.parse(lines).test {
            val first = awaitItem()
            assertThat(first).isInstanceOf(StreamEvent.ContentDelta::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
