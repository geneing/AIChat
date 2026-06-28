package com.github.geneing.aichat

import android.app.Application
import com.github.geneing.aichat.core.data.AppInitializer
import com.github.geneing.aichat.core.di.AppScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AIChatApp : Application() {

    @Inject
    @AppScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()
        appScope.launch { appInitializer.run() }
    }
}
