package com.eugene.aichat.core.di

import android.content.Context
import androidx.room.Room
import com.eugene.aichat.core.data.db.AppDatabase
import com.eugene.aichat.core.data.db.DatabaseCallback
import com.eugene.aichat.core.data.db.dao.AgentDao
import com.eugene.aichat.core.data.db.dao.AttachmentDao
import com.eugene.aichat.core.data.db.dao.ChatDao
import com.eugene.aichat.core.data.db.dao.MessageDao
import com.eugene.aichat.core.data.db.dao.ModelConfigDao
import com.eugene.aichat.core.data.db.dao.PendingActionDao
import com.eugene.aichat.core.data.db.dao.SkillDao
import com.eugene.aichat.core.data.repository.AgentRepository
import com.eugene.aichat.core.data.repository.AgentRepositoryImpl
import com.eugene.aichat.core.data.repository.ChatRepository
import com.eugene.aichat.core.data.repository.ChatRepositoryImpl
import com.eugene.aichat.core.data.repository.ModelConfigRepository
import com.eugene.aichat.core.data.repository.ModelConfigRepositoryImpl
import com.eugene.aichat.core.data.repository.SkillRepository
import com.eugene.aichat.core.data.repository.SkillRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreDataModuleProviders {

    @Provides
    @Singleton
    fun provideJson(): Json = com.eugene.aichat.core.data.serialization.AppJson

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "aichat.db"
    )
        .addCallback(DatabaseCallback())
        .fallbackToDestructiveMigration()
        .build()

    @Provides fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()
    @Provides fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()
    @Provides fun provideAttachmentDao(db: AppDatabase): AttachmentDao = db.attachmentDao()
    @Provides fun provideModelConfigDao(db: AppDatabase): ModelConfigDao = db.modelConfigDao()
    @Provides fun provideSkillDao(db: AppDatabase): SkillDao = db.skillDao()
    @Provides fun provideAgentDao(db: AppDatabase): AgentDao = db.agentDao()
    @Provides fun providePendingActionDao(db: AppDatabase): PendingActionDao = db.pendingActionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreDataModuleBindings {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindModelConfigRepository(impl: ModelConfigRepositoryImpl): ModelConfigRepository

    @Binds
    @Singleton
    abstract fun bindSkillRepository(impl: SkillRepositoryImpl): SkillRepository

    @Binds
    @Singleton
    abstract fun bindAgentRepository(impl: AgentRepositoryImpl): AgentRepository
}
