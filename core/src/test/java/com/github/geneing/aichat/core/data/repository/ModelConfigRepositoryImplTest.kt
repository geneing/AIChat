package com.github.geneing.aichat.core.data.repository

import com.github.geneing.aichat.core.data.db.dao.ModelConfigDao
import com.github.geneing.aichat.core.data.db.entity.ModelConfigEntity
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.ProviderType
import com.github.geneing.aichat.core.security.EncryptedKeyStore
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModelConfigRepositoryImplTest {

    @Test
    fun `upsert stores API key in EncryptedKeyStore and toggles default`() = runTest(UnconfinedTestDispatcher()) {
        val dao = FakeModelConfigDao()
        val keyStore = mockk<EncryptedKeyStore>(relaxed = true) {
            every { getApiKey("m1") } returns "sk-abc"
        }
        val repo = ModelConfigRepositoryImpl(dao, keyStore)

        val config = ModelConfig(
            id = "m1",
            displayName = "GPT-4o",
            providerType = ProviderType.OPENAI,
            baseUrl = "https://api.openai.com/v1",
            model = "gpt-4o",
            apiKey = "sk-abc",
            temperature = 0.5f,
            topP = 1f,
            maxTokens = 4096,
            supportsTools = true,
            supportsVision = true,
            supportsAudio = false,
            isDefault = true
        )
        repo.upsert(config)

        verify { keyStore.putApiKey("m1", "sk-abc") }
        val saved = dao.getById("m1")
        assertThat(saved).isNotNull()
        // Token in the column is opaque; the real key is fetched from the key store.
        assertThat(saved!!.apiKeyEncrypted).isEqualTo("enc:api-key")

        val resolved = repo.getById("m1")
        assertThat(resolved?.apiKey).isEqualTo("sk-abc")
        assertThat(resolved?.isDefault).isTrue()
    }

    @Test
    fun `delete clears API key and row`() = runTest(UnconfinedTestDispatcher()) {
        val dao = FakeModelConfigDao()
        val keyStore = mockk<EncryptedKeyStore>(relaxed = true)
        val repo = ModelConfigRepositoryImpl(dao, keyStore)

        repo.upsert(
            ModelConfig(
                id = "m1", displayName = "X", providerType = ProviderType.OPENAI,
                baseUrl = "u", model = "m", apiKey = "k",
                temperature = 0f, topP = 1f, maxTokens = 1,
                supportsTools = false, supportsVision = false, supportsAudio = false, isDefault = false
            )
        )
        repo.deleteById("m1")
        verify { keyStore.removeApiKey("m1") }
        assertThat(dao.getById("m1")).isNull()
    }

    @Test
    fun `observeDefault emits the model marked default`() = runTest(UnconfinedTestDispatcher()) {
        val dao = FakeModelConfigDao()
        val keyStore = mockk<EncryptedKeyStore>(relaxed = true) {
            every { getApiKey("m1") } returns "k1"
        }
        val repo = ModelConfigRepositoryImpl(dao, keyStore)
        repo.upsert(
            ModelConfig(
                id = "m1", displayName = "A", providerType = ProviderType.OPENAI,
                baseUrl = "u", model = "m", apiKey = "k1",
                temperature = 0f, topP = 1f, maxTokens = 1,
                supportsTools = true, supportsVision = false, supportsAudio = false, isDefault = true
            )
        )
        val def = repo.observeDefault().first()
        assertThat(def?.id).isEqualTo("m1")
    }
}

private class FakeModelConfigDao : ModelConfigDao {
    private val map = mutableMapOf<String, ModelConfigEntity>()
    private val flow = MutableStateFlow<List<ModelConfigEntity>>(emptyList())

    override fun observeAll(): Flow<List<ModelConfigEntity>> = flow
    override fun observeByProvider(providerType: String): Flow<List<ModelConfigEntity>> = flow
    override fun observeDefault(): Flow<ModelConfigEntity?> = MutableStateFlow(
        map.values.firstOrNull { it.isDefault }
    )
    override suspend fun getById(id: String): ModelConfigEntity? = map[id]

    override suspend fun upsert(model: ModelConfigEntity) {
        map[model.id] = model
        flow.value = map.values.toList()
        (observeDefault() as MutableStateFlow).value = map.values.firstOrNull { it.isDefault }
    }

    override suspend fun clearDefaults() {
        for (k in map.keys.toList()) map[k] = map[k]!!.copy(isDefault = false)
        flow.value = map.values.toList()
        (observeDefault() as MutableStateFlow).value = null
    }

    override suspend fun setDefault(id: String, ts: Long) {
        clearDefaults()
        map[id] = map[id]!!.copy(isDefault = true, updatedAt = ts)
        flow.value = map.values.toList()
        (observeDefault() as MutableStateFlow).value = map[id]
    }

    override suspend fun deleteById(id: String) {
        map.remove(id)
        flow.value = map.values.toList()
        (observeDefault() as MutableStateFlow).value = map.values.firstOrNull { it.isDefault }
    }
}
