package com.eugene.aichat.core.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val store: DataStore<Preferences>
) {
    val themeMode: Flow<String> = store.data.map { it[Keys.THEME_MODE] ?: ThemeModeDefault.SYSTEM }
    val defaultModelId: Flow<String?> = store.data.map { it[Keys.DEFAULT_MODEL_ID] }
    val defaultAgentId: Flow<String?> = store.data.map { it[Keys.DEFAULT_AGENT_ID] }
    val voiceAutoSpeak: Flow<Boolean> = store.data.map { it[Keys.VOICE_AUTO_SPEAK] ?: true }
    val webSearchProvider: Flow<String> = store.data.map { it[Keys.WEB_SEARCH_PROVIDER] ?: WebSearchProviderDefault.NONE }
    val webSearchApiKey: Flow<String?> = store.data.map { it[Keys.WEB_SEARCH_API_KEY] }
    val shareLocation: Flow<Boolean> = store.data.map { it[Keys.SHARE_LOCATION] ?: false }

    suspend fun setThemeMode(value: String) { store.edit { it[Keys.THEME_MODE] = value } }
    suspend fun setDefaultModelId(value: String?) { store.edit { if (value == null) it.remove(Keys.DEFAULT_MODEL_ID) else it[Keys.DEFAULT_MODEL_ID] = value } }
    suspend fun setDefaultAgentId(value: String?) { store.edit { if (value == null) it.remove(Keys.DEFAULT_AGENT_ID) else it[Keys.DEFAULT_AGENT_ID] = value } }
    suspend fun setVoiceAutoSpeak(value: Boolean) { store.edit { it[Keys.VOICE_AUTO_SPEAK] = value } }
    suspend fun setWebSearchProvider(value: String) { store.edit { it[Keys.WEB_SEARCH_PROVIDER] = value } }
    suspend fun setWebSearchApiKey(value: String?) { store.edit { if (value.isNullOrEmpty()) it.remove(Keys.WEB_SEARCH_API_KEY) else it[Keys.WEB_SEARCH_API_KEY] = value } }
    suspend fun setShareLocation(value: Boolean) { store.edit { it[Keys.SHARE_LOCATION] = value } }
}

private object Keys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DEFAULT_MODEL_ID = stringPreferencesKey("default_model_id")
    val DEFAULT_AGENT_ID = stringPreferencesKey("default_agent_id")
    val VOICE_AUTO_SPEAK = androidx.datastore.preferences.core.booleanPreferencesKey("voice_auto_speak")
    val WEB_SEARCH_PROVIDER = stringPreferencesKey("web_search_provider")
    val WEB_SEARCH_API_KEY = stringPreferencesKey("web_search_api_key")
    val SHARE_LOCATION = androidx.datastore.preferences.core.booleanPreferencesKey("share_location")
}

private object ThemeModeDefault { const val SYSTEM = "SYSTEM" }
private object WebSearchProviderDefault { const val NONE = "none" }
