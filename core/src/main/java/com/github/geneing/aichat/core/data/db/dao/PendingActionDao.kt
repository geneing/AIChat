package com.github.geneing.aichat.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.geneing.aichat.core.data.db.entity.PendingActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingActionDao {

    @Query("SELECT * FROM pending_actions WHERE messageId = :messageId ORDER BY createdAt ASC")
    fun observeForMessage(messageId: String): Flow<List<PendingActionEntity>>

    @Query("SELECT * FROM pending_actions WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun observePending(): Flow<List<PendingActionEntity>>

    @Upsert
    suspend fun upsert(action: PendingActionEntity)

    @Query("UPDATE pending_actions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deleteById(id: String)
}
