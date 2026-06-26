package com.eugene.aichat.core.data.repository

import com.eugene.aichat.core.data.db.dao.AttachmentDao
import com.eugene.aichat.core.data.db.dao.ChatDao
import com.eugene.aichat.core.data.db.dao.MessageDao
import com.eugene.aichat.core.domain.model.Chat
import com.eugene.aichat.core.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val attachmentDao: AttachmentDao
) : ChatRepository {

    override fun observeChats(): Flow<List<Chat>> =
        chatDao.observeChats().map { rows -> rows.map { it.toDomain() } }

    override fun observeChat(id: String): Flow<Chat?> =
        chatDao.observeChat(id).map { it?.toDomain() }

    override fun observeMessages(chatId: String): Flow<List<Message>> {
        val messageFlow = messageDao.observeForChat(chatId)
        return messageFlow.map { messages ->
            val ids = messages.map { it.id }
            val attachments = if (ids.isEmpty()) emptyList() else attachmentDao.getForMessages(ids)
            val byMessage = attachments.groupBy { it.messageId }
            messages.map { m ->
                m.toDomain(attachments = byMessage[m.id].orEmpty().map { it.toDomain() })
            }
        }
    }

    override suspend fun getChat(id: String): Chat? = chatDao.getChat(id)?.toDomain()

    override suspend fun createChat(chat: Chat) {
        chatDao.upsert(chat.toEntity())
    }

    override suspend fun updateTitle(chatId: String, title: String) {
        chatDao.updateTitle(chatId, title, System.currentTimeMillis())
    }

    override suspend fun touch(chatId: String) {
        chatDao.touch(chatId, System.currentTimeMillis())
    }

    override suspend fun setArchived(chatId: String, archived: Boolean) {
        chatDao.setArchived(chatId, archived, System.currentTimeMillis())
    }

    override suspend fun deleteChat(chatId: String) {
        chatDao.deleteById(chatId)
    }

    override suspend fun appendMessage(message: Message) {
        messageDao.upsert(message.toEntity())
    }

    override suspend fun updateMessageContent(messageId: String, text: String?, streaming: Boolean) {
        messageDao.updateContent(messageId, text, streaming)
    }

    override suspend fun updateMessageThinking(messageId: String, text: String?) {
        messageDao.updateThinking(messageId, text)
    }

    override suspend fun markMessageComplete(messageId: String, latencyMs: Long?) {
        messageDao.markComplete(messageId, latencyMs)
    }
}
