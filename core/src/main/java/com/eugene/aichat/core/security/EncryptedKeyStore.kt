package com.eugene.aichat.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores API keys in EncryptedSharedPreferences so the keys never appear
 * in plaintext in the Room database.
 *
 * Room stores an opaque token like "enc:<modelId>" to indicate that the
 * secret lives in this store. The repository resolves the token to the
 * real key on demand.
 */
@Singleton
class EncryptedKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        runCatching {
            EncryptedSharedPreferences.create(
                context,
                "aichat_api_keys",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse {
            // Some emulators / devices lack hardware-backed keystore.
            // Fall back to a non-encrypted prefs file so the app still
            // runs. Keys will at least be obfuscated by app sandboxing.
            context.getSharedPreferences("aichat_api_keys_fallback", Context.MODE_PRIVATE)
        }
    }

    fun putApiKey(modelId: String, key: String) {
        prefs.edit().putString(keyFor(modelId), key).apply()
    }

    fun getApiKey(modelId: String): String? = prefs.getString(keyFor(modelId), null)

    fun removeApiKey(modelId: String) {
        prefs.edit().remove(keyFor(modelId)).apply()
    }

    fun hasApiKey(modelId: String): Boolean = prefs.contains(keyFor(modelId))

    private fun keyFor(modelId: String): String = "apikey:$modelId"
}
