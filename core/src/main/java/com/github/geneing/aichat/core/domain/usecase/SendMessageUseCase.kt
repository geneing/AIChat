package com.github.geneing.aichat.core.domain.usecase

import com.github.geneing.aichat.core.data.repository.ChatRepository
import com.github.geneing.aichat.core.data.repository.ModelConfigRepository
import com.github.geneing.aichat.core.domain.model.Chat
import com.github.geneing.aichat.core.domain.model.Message
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.Role
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/**
 * Appends a user message to a chat (creating the chat if it doesn't
 * exist) and returns a [SendResult] describing what was created.
 *
 * The assistant message placeholder is also created here so the UI
 * has a stable id to bind streaming content into.
 */
class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val modelConfigRepository: ModelConfigRepository
) {
    suspend operator fun invoke(
        requestedChatId: String?,
        text: String,
        agentId: String? = null
    ): SendResult {
        val chat = resolveChat(requestedChatId, agentId)
        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chat.id,
            role = Role.USER,
            contentText = text,
            createdAt = System.currentTimeMillis()
        )
        chatRepository.appendMessage(userMessage)
        chatRepository.touch(chat.id)

        val assistantId = UUID.randomUUID().toString()
        val assistantPlaceholder = Message(
            id = assistantId,
            chatId = chat.id,
            role = Role.ASSISTANT,
            contentText = "",
            isStreaming = true,
            createdAt = System.currentTimeMillis()
        )
        chatRepository.appendMessage(assistantPlaceholder)

        val model = modelConfigRepository.getById(chat.modelConfigId)
            ?: error("Model config ${chat.modelConfigId} not found")

        return SendResult(
            chatId = chat.id,
            userMessageId = userMessage.id,
            assistantMessageId = assistantId,
            model = model
        )
    }

    private suspend fun resolveChat(requestedChatId: String?, agentId: String?): Chat {
        if (requestedChatId.isNullOrBlank() || requestedChatId == NEW_CHAT_SENTINEL) {
            val model = modelConfigRepository.observeDefault().first()
                ?: modelConfigRepository.observeAll().first().firstOrNull()
                ?: error("No model configured. Add one in Settings.")
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val chat = Chat(
                id = id,
                title = "New chat",
                agentId = agentId?.takeIf { it.isNotBlank() },
                modelConfigId = model.id,
                createdAt = now,
                updatedAt = now
            )
            chatRepository.createChat(chat)
            return chat
        }
        val existing = chatRepository.getChat(requestedChatId)
            ?: error("Chat $requestedChatId not found")
        if (existing.agentId == null && !agentId.isNullOrBlank()) {
            val bound = existing.copy(agentId = agentId)
            chatRepository.createChat(bound)
            return bound
        }
        return existing
    }

    companion object {
        const val NEW_CHAT_SENTINEL = "new"
    }
}

data class SendResult(
    val chatId: String,
    val userMessageId: String,
    val assistantMessageId: String,
    val model: ModelConfig
)
