package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.OrderProductItem
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.OrderProductItemRepository

class OrderProductItemRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : OrderProductItemRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getItemsByOrderId(orderId: String): DataResult<List<OrderProductItem>> {
        return try {
            val result = postgrest
                .from("order_product_items")
                .select { filter { eq("order_id", orderId) } }
                .decodeList<OrderProductItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get order items!")
        }
    }

    override suspend fun getItemById(itemId: String): DataResult<OrderProductItem> {
        return try {
            val result = postgrest
                .from("order_product_items")
                .select { filter { eq("id", itemId) } }
                .decodeSingle<OrderProductItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get order item!")
        }
    }

    override suspend fun addItem(orderProductItem: OrderProductItem): DataResult<OrderProductItem> {
        return try {
            val result = postgrest
                .from("order_product_items")
                .insert(orderProductItem) { select() }
                .decodeSingle<OrderProductItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to add order item!")
        }
    }

    override suspend fun updateItem(itemId: String, orderProductItem: OrderProductItem): DataResult<OrderProductItem> {
        return try {
            val result = postgrest
                .from("order_product_items")
                .update(orderProductItem) {
                    filter { eq("id", itemId) }
                    select()
                }
                .decodeSingle<OrderProductItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update order item!")
        }
    }

    override suspend fun updateItemQuantity(itemId: String, quantity: Int): DataResult<OrderProductItem> {
        return try {
            val result = postgrest
                .from("order_product_items")
                .update({ set("quantity", quantity) }) {
                    filter { eq("id", itemId) }
                    select()
                }
                .decodeSingle<OrderProductItem>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update item quantity!")
        }
    }
}