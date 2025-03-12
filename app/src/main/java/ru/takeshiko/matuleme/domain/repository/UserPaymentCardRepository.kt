package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserPaymentCardRepository {
    suspend fun getPaymentCardsByUserId(userId: String): DataResult<List<UserPaymentCard>>
    suspend fun addPaymentCard(userPaymentCard: UserPaymentCard): DataResult<UserPaymentCard>
    suspend fun updatePaymentCardWithReset(userId: String, paymentCardId: String, address: UserPaymentCard): DataResult<UserPaymentCard>
    suspend fun updatePaymentCard(userId: String, paymentCardId: String, address: UserPaymentCard): DataResult<UserPaymentCard>
    suspend fun removePaymentCard(userId: String, paymentCardId: String): DataResult<String>
    suspend fun resetDefaultPaymentCard(userId: String): DataResult<Unit>
}