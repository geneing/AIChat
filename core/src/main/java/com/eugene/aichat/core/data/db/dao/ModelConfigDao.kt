package com.eugene.aichat.core.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eugene.aichat.core.data.db.entity.ModelConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelConfigDao {

    @Query("SELECT * FROM model_configs ORDER BY displayName ASC")
    fun observeAll(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs WHERE providerType = :providerType ORDER BY displayName ASC")
    fun observeByProvider(providerType: String): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs WHERE isDefault = 1 LIMIT 1")
    fun observeDefault(): Flow<ModelConfigEntity?>

    @Query("SELECT * FROM model_configs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ModelConfigEntity?

    @Upsert
    suspend fun upsert(model: ModelConfigEntity)

    @Query("UPDATE model_configs SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("UPDATE model_configs SET isDefault = 1, updatedAt = :ts WHERE id = :id")
    suspend fun setDefault(id: String, ts: Long)

    @Query("DELETE FROM model_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
