package com.eugene.aichat.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eugene.aichat.core.data.db.dao.AgentDao
import com.eugene.aichat.core.data.db.dao.AttachmentDao
import com.eugene.aichat.core.data.db.dao.ChatDao
import com.eugene.aichat.core.data.db.dao.MessageDao
import com.eugene.aichat.core.data.db.dao.ModelConfigDao
import com.eugene.aichat.core.data.db.dao.PendingActionDao
import com.eugene.aichat.core.data.db.dao.SkillDao
import com.eugene.aichat.core.data.db.entity.AgentEntity
import com.eugene.aichat.core.data.db.entity.AttachmentEntity
import com.eugene.aichat.core.data.db.entity.ChatEntity
import com.eugene.aichat.core.data.db.entity.MessageEntity
import com.eugene.aichat.core.data.db.entity.ModelConfigEntity
import com.eugene.aichat.core.data.db.entity.PendingActionEntity
import com.eugene.aichat.core.data.db.entity.SkillEntity
import com.eugene.aichat.core.data.db.entity.SourceRefEntity

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
