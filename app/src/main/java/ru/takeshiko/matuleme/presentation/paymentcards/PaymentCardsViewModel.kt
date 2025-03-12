package ru.takeshiko.matuleme.presentation.paymentcards

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

class PaymentCardsViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _paymentCardsResult = MutableLiveData<DataResult<List<UserPaymentCard>>>()
    val paymentCardsResult: LiveData<DataResult<List<UserPaymentCard>>> = _paymentCardsResult

    private val userPaymentCardRepository = UserPaymentCardRepositoryImpl(supabaseClientManager)

    fun loadUserPaymentCards() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userPaymentCardRepository.getPaymentCardsByUserId(userId)) {
                    is DataResult.Success -> {
                        val sortedPaymentCards = result.data.sortedByDescending { it.isDefault }
                        _paymentCardsResult.value = DataResult.Success(sortedPaymentCards)
                    }
                    is DataResult.Error -> _paymentCardsResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun removePaymentCard(paymentCard: UserPaymentCard) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userPaymentCardRepository.removePaymentCard(userId, paymentCard.id!!)) {
                    is DataResult.Success -> {
                        val updatedPaymentCards = _paymentCardsResult.value?.let { dataResult ->
                            if (dataResult is DataResult.Success) {
                                dataResult.data.filter { it.id != paymentCard.id }
                            } else {
                                emptyList()
                            }
                        } ?: emptyList()

                        if (paymentCard.isDefault && updatedPaymentCards.isNotEmpty()) {
                            setDefaultPaymentCard(updatedPaymentCards.first())
                        } else {
                            _paymentCardsResult.value = DataResult.Success(updatedPaymentCards.sortedByDescending { it.isDefault })
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun setDefaultPaymentCard(paymentCard: UserPaymentCard) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (userPaymentCardRepository.resetDefaultPaymentCard(userId)) {
                    is DataResult.Success -> {
                        val updatedPaymentCard = paymentCard.copy(isDefault = true)
                        when (val updateResult = userPaymentCardRepository.updatePaymentCardWithReset(userId, updatedPaymentCard.id!!, updatedPaymentCard)) {
                            is DataResult.Success -> loadUserPaymentCards()
                            is DataResult.Error -> Log.d(javaClass.name, updateResult.message)
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, "Failed to reset default payment card!")
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}