package com.github.geneing.aichat.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.geneing.aichat.core.data.db.entity.SourceRefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceRefDao {

    @Query("SELECT * FROM source_refs WHERE messageId IN (:messageIds) ORDER BY id ASC")
    suspend fun getForMessages(messageIds: List<String>): List<SourceRefEntity>

    @Query("SELECT * FROM source_refs WHERE messageId = :messageId ORDER BY id ASC")
    fun observeForMessage(messageId: String): Flow<List<SourceRefEntity>>

    @Upsert
    suspend fun upsert(ref: SourceRefEntity)

    @Upsert
    suspend fun upsertAll(refs: List<SourceRefEntity>)

    @Query("DELETE FROM source_refs WHERE messageId = :messageId")
    suspend fun deleteForMessage(messageId: String)
}
