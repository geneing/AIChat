package com.github.geneing.aichat.core.ai.tools

import android.content.Context
import com.github.geneing.aichat.core.ai.location.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [WebSearchProvider] used until a key is configured in
 * Settings. The settings module (step 4.5) replaces this binding with
 * a SerpAPI- or Brave-backed implementation when a key is present.
 */
@Singleton
class NoopWebSearchProvider @Inject constructor() : WebSearchProvider {
    override val enabled: Boolean = false
    override suspend fun search(query: String, topK: Int): List<WebSearchResult> = emptyList()
}

/**
 * Default [ToolContext] that exposes an [android.content.Context], an
 * empty chatId, and the no-op location/search providers. The runtime
 * substitutes a richer context when it has one.
 */
@Singleton
class DefaultToolContext @Inject constructor(
    @ApplicationContext private val context: Context,
    override val locationProvider: LocationProvider,
    override val webSearchProvider: WebSearchProvider
) : ToolContext {
    override val caller: Context = context
    override val chatId: String? = null
}
