package com.eugene.aichat.core.data

import com.eugene.aichat.core.data.repository.AgentRepository
import com.eugene.aichat.core.data.repository.SkillRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triggers first-run seeding for skills and agents. Called from the Application
 * on a background coroutine so the first chat screen shows the seeded list
 * without a blocking round-trip on the UI thread.
 */
@Singleton
class AppInitializer @Inject constructor(
    private val skillRepository: SkillRepository,
    private val agentRepository: AgentRepository
) {
    suspend fun run() {
        skillRepository.ensureSeeded()
        agentRepository.ensureSeeded()
    }
}
