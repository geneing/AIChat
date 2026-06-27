package com.eugene.aichat.core.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eugene.aichat.core.data.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun observeForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun pagingForChat(chatId: String): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageEntity?

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Query("UPDATE messages SET contentText = :text, isStreaming = :streaming WHERE id = :id")
    suspend fun updateContent(id: String, text: String?, streaming: Boolean)

    @Query("UPDATE messages SET thinkingText = :text WHERE id = :id")
    suspend fun updateThinking(id: String, text: String?)

    @Query("UPDATE messages SET isStreaming = 0, latencyMs = :latencyMs WHERE id = :id")
    suspend fun markComplete(id: String, latencyMs: Long?)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteForChat(chatId: String)
}
