package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserPaymentCardRepository

class UserPaymentCardRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserPaymentCardRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getPaymentCardsByUserId(userId: String): DataResult<List<UserPaymentCard>> {
        return try {
            val result = postgrest
                .from("user_payment_cards")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserPaymentCard>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user payment cards!")
        }
    }

    override suspend fun addPaymentCard(userPaymentCard: UserPaymentCard): DataResult<UserPaymentCard> {
        return try {
            val result = postgrest
                .from("user_payment_cards")
                .insert(userPaymentCard) { select() }
                .decodeSingle<UserPaymentCard>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to add user payment cards!")
        }
    }

    override suspend fun updatePaymentCard(userId: String, paymentCardId: String, paymentCard: UserPaymentCard): DataResult<UserPaymentCard> {
        return try {
            val result = postgrest
                .from("user_payment_cards")
                .update(paymentCard) {
                    filter { eq("id", paymentCardId) }
                    select()
                }
                .decodeSingle<UserPaymentCard>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update user payment cards!")
        }
    }

    override suspend fun updatePaymentCardWithReset(userId: String, paymentCardId: String, address: UserPaymentCard): DataResult<UserPaymentCard> {
        return try {
            resetDefaultPaymentCard(userId)
            val result = postgrest
                .from("user_payment_cards")
                .update(address) {
                    filter { eq("id", paymentCardId) }
                    select()
                }
                .decodeSingle<UserPaymentCard>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update user payment cards!")
        }
    }

    override suspend fun removePaymentCard(userId: String, paymentCardId: String): DataResult<String> {
        return try {
            postgrest
                .from("user_payment_cards")
                .delete {
                    filter {
                        eq("id", paymentCardId)
                        eq("user_id", userId)
                    }
                }
            DataResult.Success(paymentCardId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to remove user payment cards!")
        }
    }

    override suspend fun resetDefaultPaymentCard(userId: String): DataResult<Unit> {
        return try {
            postgrest
                .from("user_payment_cards")
                .update({ set("is_default", false) })
                { filter { eq("user_id", userId) } }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to reset default payment card!")
        }
    }
}