package ru.takeshiko.matuleme.presentation.editpaymentcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager

class EditPaymentCardViewModelFactory(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPaymentCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditPaymentCardViewModel(supabaseClientManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}