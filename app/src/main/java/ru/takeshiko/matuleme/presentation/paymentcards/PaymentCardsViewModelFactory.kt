package ru.takeshiko.matuleme.presentation.paymentcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class PaymentCardsViewModelFactory(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentCardsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentCardsViewModel(supabaseClientManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}