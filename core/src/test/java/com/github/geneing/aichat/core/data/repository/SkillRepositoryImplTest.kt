package com.github.geneing.aichat.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.github.geneing.aichat.core.domain.model.Skill
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SkillRepositoryImplTest {

    @Test
    fun `upsert stores skill with csv tags`() = runTest(UnconfinedTestDispatcher()) {
        val fakeDao = FakeSkillDao()
        val store = InMemoryPreferencesDataStore()
        val repo = SkillRepositoryImpl(
            dao = fakeDao,
            seedLoader = emptySeedLoader(),
            store = store
        )
        val skill = Skill(
            id = "skill.test",
            name = "Test",
            description = "d",
            systemPrompt = "sp",
            body = "body",
            tags = listOf("alpha", "beta"),
            toolAllowList = listOf("open_url"),
            isBuiltIn = false,
            isEnabled = true,
            version = 1
        )
        repo.upsert(skill)
        val loaded = repo.observeAll().first()
        assertThat(loaded).hasSize(1)
        assertThat(loaded[0].tags).containsExactly("alpha", "beta").inOrder()
        assertThat(loaded[0].toolAllowList).containsExactly("open_url")
    }

    @Test
    fun `setEnabled toggles the skill`() = runTest(UnconfinedTestDispatcher()) {
        val fakeDao = FakeSkillDao()
        val repo = SkillRepositoryImpl(fakeDao, emptySeedLoader(), InMemoryPreferencesDataStore())
        repo.upsert(
            Skill(
                id = "skill.t", name = "T", description = "", systemPrompt = "",
                body = "", tags = emptyList(), toolAllowList = emptyList(),
                isBuiltIn = false, isEnabled = true, version = 1
            )
        )
        repo.setEnabled("skill.t", false)
        val enabled = repo.observeEnabled().first()
        assertThat(enabled).isEmpty()
    }

    @Test
    fun `ensureSeeded inserts new skills and is idempotent`() = runTest(UnconfinedTestDispatcher()) {
        val fakeDao = FakeSkillDao()
        val seedLoader = mockk<com.github.geneing.aichat.core.data.seed.AssetSeedLoader> {
            coEvery { loadSkills() } returns emptyList()
        }
        val repo = SkillRepositoryImpl(fakeDao, seedLoader, InMemoryPreferencesDataStore())
        repo.ensureSeeded()
        repo.ensureSeeded()
        assertThat(repo.observeAll().first()).isEmpty()
    }

    private fun emptySeedLoader() = mockk<com.github.geneing.aichat.core.data.seed.AssetSeedLoader>(relaxed = true) {
        coEvery { loadSkills() } returns emptyList()
    }
}

private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val flow = MutableStateFlow<Preferences>(mutablePreferencesOf())
    override val data: Flow<Preferences> = flow
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val next = transform(flow.value)
        flow.value = next
        return next
    }
}

private class FakeSkillDao : com.github.geneing.aichat.core.data.db.dao.SkillDao {
    private val store = mutableMapOf<String, com.github.geneing.aichat.core.data.db.entity.SkillEntity>()

    override fun observeAll(): Flow<List<com.github.geneing.aichat.core.data.db.entity.SkillEntity>> =
        MutableStateFlow(store.values.toList())

    override fun observeEnabled(): Flow<List<com.github.geneing.aichat.core.data.db.entity.SkillEntity>> =
        MutableStateFlow(store.values.filter { it.isEnabled })

    override suspend fun getById(id: String): com.github.geneing.aichat.core.data.db.entity.SkillEntity? =
        store[id]

    override suspend fun listIds(): List<String> = store.keys.toList()

    override suspend fun upsert(skill: com.github.geneing.aichat.core.data.db.entity.SkillEntity) {
        store[skill.id] = skill
        (observeAll() as MutableStateFlow).value = store.values.toList()
        (observeEnabled() as MutableStateFlow).value = store.values.filter { it.isEnabled }
    }

    override suspend fun setEnabled(id: String, enabled: Boolean, ts: Long) {
        val cur = store[id] ?: return
        store[id] = cur.copy(isEnabled = enabled, updatedAt = ts)
        (observeAll() as MutableStateFlow).value = store.values.toList()
        (observeEnabled() as MutableStateFlow).value = store.values.filter { it.isEnabled }
    }

    override suspend fun deleteUserSkill(id: String) {
        if (store[id]?.isBuiltIn == true) return
        store.remove(id)
        (observeAll() as MutableStateFlow).value = store.values.toList()
        (observeEnabled() as MutableStateFlow).value = store.values.filter { it.isEnabled }
    }
}
