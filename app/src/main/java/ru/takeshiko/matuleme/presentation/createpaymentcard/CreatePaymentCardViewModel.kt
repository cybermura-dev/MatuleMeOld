package ru.takeshiko.matuleme.presentation.createpaymentcard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserPaymentCardRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard
import ru.takeshiko.matuleme.domain.models.result.DataResult

class CreatePaymentCardViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val userPaymentCardRepository = UserPaymentCardRepositoryImpl(supabaseClientManager)

    fun savePaymentCard(newNumber: String, newHolder: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val paymentCardsResult = userPaymentCardRepository.getPaymentCardsByUserId(userId)) {
                    is DataResult.Success -> {
                        val existingPaymentCards = paymentCardsResult.data
                        val isFirstPaymentCard = existingPaymentCards.isEmpty()

                        val paymentCardToSave = UserPaymentCard(
                            userId = userId,
                            number = newNumber,
                            holder = newHolder,
                            isDefault = isFirstPaymentCard
                        )

                        when (val saveResult = userPaymentCardRepository.addPaymentCard(paymentCardToSave)) {
                            is DataResult.Success -> {
                                if (!isFirstPaymentCard) {
                                    existingPaymentCards.forEach {
                                        userPaymentCardRepository.updatePaymentCard(userId, it.id!!, it)
                                    }
                                }
                            }
                            is DataResult.Error -> Log.d(javaClass.name, saveResult.message)
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, paymentCardsResult.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}