package ru.takeshiko.matuleme.presentation.editpaymentcard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserPaymentCardRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard
import ru.takeshiko.matuleme.domain.models.result.DataResult

class EditPaymentCardViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val userPaymentCardRepository = UserPaymentCardRepositoryImpl(supabaseClientManager)

    private val _paymentCard = MutableLiveData<UserPaymentCard?>()
    val paymentCard: LiveData<UserPaymentCard?> = _paymentCard

    fun loadPaymentCard(paymentCardId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userPaymentCardRepository.getPaymentCardsByUserId(userId)) {
                    is DataResult.Success -> {
                        val foundPaymentCard = result.data.find { it.id == paymentCardId }
                        _paymentCard.value = foundPaymentCard
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun updatePaymentCard(paymentCardId: String, newNumber: String, newHolder: String, isDefault: Boolean) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                val updatedPaymentCard = _paymentCard.value?.copy(number = newNumber, holder = newHolder, isDefault = isDefault)
                if (updatedPaymentCard != null) {
                    when (val result = userPaymentCardRepository.updatePaymentCard(userId, paymentCardId, updatedPaymentCard)) {
                        is DataResult.Success -> Log.d(javaClass.name, "Payment card updated successfully!")
                        is DataResult.Error -> Log.d(javaClass.name, result.message)
                    }
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}