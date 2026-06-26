package com.eugene.aichat.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.eugene.aichat.core.data.db.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    fun observeChat(id: String): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    suspend fun getChat(id: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(chat: ChatEntity): Long

    @Upsert
    suspend fun upsert(chat: ChatEntity)

    @Query("UPDATE chats SET title = :title, updatedAt = :ts WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, ts: Long)

    @Query("UPDATE chats SET updatedAt = :ts WHERE id = :id")
    suspend fun touch(id: String, ts: Long)

    @Query("UPDATE chats SET isArchived = :archived, updatedAt = :ts WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, ts: Long)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteById(id: String)
}
