package com.github.geneing.aichat.core.data.repository

import com.github.geneing.aichat.core.domain.model.Chat
import com.github.geneing.aichat.core.domain.model.Message
import com.github.geneing.aichat.core.domain.model.SourceRef
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(): Flow<List<Chat>>
    fun observeChat(id: String): Flow<Chat?>
    fun observeMessages(chatId: String): Flow<List<Message>>
    fun observeSources(messageId: String): Flow<List<SourceRef>>
    suspend fun getChat(id: String): Chat?
    suspend fun createChat(chat: Chat)
    suspend fun updateTitle(chatId: String, title: String)
    suspend fun touch(chatId: String)
    suspend fun setArchived(chatId: String, archived: Boolean)
    suspend fun deleteChat(chatId: String)
    suspend fun appendMessage(message: Message)
    suspend fun updateMessageContent(messageId: String, text: String?, streaming: Boolean)
    suspend fun updateMessageThinking(messageId: String, text: String?)
    suspend fun markMessageComplete(messageId: String, latencyMs: Long?)
    suspend fun addSources(messageId: String, sources: List<SourceRef>)
}
