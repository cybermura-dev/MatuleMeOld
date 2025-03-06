package ru.takeshiko.matuleme.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class SplashViewModelFactory(
    private val appPreferencesManager: AppPreferencesManager,
    private val supabaseClientManager: SupabaseClientManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(
                appPreferencesManager,
                supabaseClientManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}