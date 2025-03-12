package ru.takeshiko.matuleme.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class MainViewModelFactory(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(supabaseClientManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}