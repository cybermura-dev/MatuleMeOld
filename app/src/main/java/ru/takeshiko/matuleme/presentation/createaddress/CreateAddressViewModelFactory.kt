package ru.takeshiko.matuleme.presentation.createaddress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class CreateAddressViewModelFactory(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateAddressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateAddressViewModel(supabaseClientManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}