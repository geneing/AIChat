package com.github.geneing.aichat.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.geneing.aichat.core.data.db.dao.AgentDao
import com.github.geneing.aichat.core.data.db.dao.AttachmentDao
import com.github.geneing.aichat.core.data.db.dao.ChatDao
import com.github.geneing.aichat.core.data.db.dao.MessageDao
import com.github.geneing.aichat.core.data.db.dao.ModelConfigDao
import com.github.geneing.aichat.core.data.db.dao.PendingActionDao
import com.github.geneing.aichat.core.data.db.dao.SkillDao
import com.github.geneing.aichat.core.data.db.entity.AgentEntity
import com.github.geneing.aichat.core.data.db.entity.AttachmentEntity
import com.github.geneing.aichat.core.data.db.entity.ChatEntity
import com.github.geneing.aichat.core.data.db.entity.MessageEntity
import com.github.geneing.aichat.core.data.db.entity.ModelConfigEntity
import com.github.geneing.aichat.core.data.db.entity.PendingActionEntity
import com.github.geneing.aichat.core.data.db.entity.SkillEntity
import com.github.geneing.aichat.core.data.db.entity.SourceRefEntity

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        AttachmentEntity::class,
        SourceRefEntity::class,
        ModelConfigEntity::class,
        SkillEntity::class,
        AgentEntity::class,
        PendingActionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun modelConfigDao(): ModelConfigDao
    abstract fun skillDao(): SkillDao
    abstract fun agentDao(): AgentDao
    abstract fun pendingActionDao(): PendingActionDao
}
