package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserOrderRepository {
    suspend fun getOrdersByUserId(userId: String): DataResult<List<UserOrder>>
    suspend fun getOrderById(orderId: String): DataResult<UserOrder>
    suspend fun getOrderByOrderNumber(orderNumber: String): DataResult<UserOrder>
    suspend fun createOrder(userOrder: UserOrder): DataResult<UserOrder>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): DataResult<UserOrder>
    suspend fun updateOrder(orderId: String, userOrder: UserOrder): DataResult<UserOrder>
}