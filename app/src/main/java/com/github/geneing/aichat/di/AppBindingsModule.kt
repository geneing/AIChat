package com.github.geneing.aichat.di

import com.github.geneing.aichat.core.ui.theme.ThemeController
import com.github.geneing.aichat.core.ui.theme.ThemeControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

    @Binds
    @Singleton
    abstract fun bindThemeController(impl: ThemeControllerImpl): ThemeController
}
