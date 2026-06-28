package com.github.geneing.aichat.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataStoreScope
