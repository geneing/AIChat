package com.eugene.aichat.core.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room callback for low-level DB lifecycle hooks.
 *
 * Built-in skills/agents are seeded by the repositories (see AssetSeedLoader),
 * not here, because Room callbacks run on the calling thread before DAOs are
 * safe to use from background coroutines.
 */
class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Marker only — seeding happens lazily in SkillRepository/AgentRepository.
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
    }
}
