package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserOrderRepository

class UserOrderRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserOrderRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getOrdersByUserId(userId: String): DataResult<List<UserOrder>> {
        return try {
            val result = postgrest
                .from("user_orders")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserOrder>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user orders!")
        }
    }

    override suspend fun getOrderById(orderId: String): DataResult<UserOrder> {
        return try {
            val result = postgrest
                .from("user_orders")
                .select { filter { eq("id", orderId) } }
                .decodeSingle<UserOrder>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get order!")
        }
    }

    override suspend fun getOrderByOrderNumber(orderNumber: String): DataResult<UserOrder> {
        return try {
            val result = postgrest
                .from("user_orders")
                .select { filter { eq("order_number", orderNumber) } }
                .decodeSingle<UserOrder>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get order by order number!")
        }
    }

    override suspend fun createOrder(userOrder: UserOrder): DataResult<UserOrder> {
        return try {
            val result = postgrest
                .from("user_orders")
                .insert(userOrder) { select() }
                .decodeSingle<UserOrder>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to create order!")
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): DataResult<UserOrder> {
        return try {
            val result = postgrest
                .from("user_orders")
                .update({ set("status", status) }) {
                    filter { eq("id", orderId) }
                    select()
                }
                .decodeSingle<UserOrder>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update order status!")
        }
    }

    override suspend fun updateOrder(orderId: String, userOrder: UserOrder): DataResult<UserOrder> {
        return try {
            val result = postgrest
                .from("user_orders")
                .update(userOrder) {
                    filter { eq("id", orderId) }
                    select()
                }
                .decodeSingle<UserOrder>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update order!")
        }
    }
}