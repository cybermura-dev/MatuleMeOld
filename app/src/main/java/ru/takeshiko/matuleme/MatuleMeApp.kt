package ru.takeshiko.matuleme

import android.app.Application
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class MatuleMeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AppPreferencesManager.initialize(this)
        SupabaseClientManager.initialize(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_KEY
        )
    }
}