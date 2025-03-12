package ru.takeshiko.matuleme.presentation.createpaymentcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class CreatePaymentCardViewModelFactory(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePaymentCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreatePaymentCardViewModel(supabaseClientManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}