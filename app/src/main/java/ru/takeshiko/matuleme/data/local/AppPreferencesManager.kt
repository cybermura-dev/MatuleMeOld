package ru.takeshiko.matuleme.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AppPreferencesManager
private constructor(context: Context) {

    val appContext: Context = context.applicationContext

    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        appContext,
        "app_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)
        set(value) = prefs.edit().putBoolean("is_first_launch", value).apply()

    fun getString(resId: Int): String = appContext.getString(resId)

    companion object {
        @Volatile
        private var instance: AppPreferencesManager? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AppPreferencesManager(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): AppPreferencesManager {
            return instance ?: throw IllegalStateException("AppPreferencesManager not initialized!")
        }
    }
}