package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserCartItemRepository

class UserCartItemRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserCartItemRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getByUserId(userId: String): DataResult<List<UserCartItem>> {
        return try {
            val result = postgrest
                .from("user_cart_items")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserCartItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user cart items!")
        }
    }

    override suspend fun get(userId: String, productId: String): DataResult<UserCartItem> {
        return try {
            val result = postgrest
                .from("user_cart_items")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("product_id", productId)
                    }
                }
                .decodeSingle<UserCartItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user cart item!")
        }
    }

    override suspend fun addCartItem(item: UserCartItem): DataResult<UserCartItem> {
        return try {
            val result = postgrest
                .from("user_cart_items")
                .insert(item) { select() }
                .decodeSingle<UserCartItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to add cart item!")
        }
    }

    override suspend fun updateQuantity(itemId: String, newQuantity: Int): DataResult<UserCartItem> {
        return try {
            val result = postgrest
                .from("user_cart_items")
                .update({ set("quantity", newQuantity) }) { select(); filter { eq("id", itemId) } }
                .decodeSingle<UserCartItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update cart item quantity!")
        }
    }

    override suspend fun removeCartItem(userId: String, itemId: String) : DataResult<String> {
        return try {
           postgrest
                .from("user_cart_items")
                .delete {
                    filter {
                        eq("product_id", itemId)
                        eq("user_id", userId)
                    }
                }
            DataResult.Success(itemId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to remove cart item!")
        }
    }
}