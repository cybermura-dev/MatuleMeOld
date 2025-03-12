package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.OrderProductItem
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface OrderProductItemRepository {
    suspend fun getItemsByOrderId(orderId: String): DataResult<List<OrderProductItem>>
    suspend fun getItemById(itemId: String): DataResult<OrderProductItem>
    suspend fun addItem(orderProductItem: OrderProductItem): DataResult<OrderProductItem>
    suspend fun updateItem(itemId: String, orderProductItem: OrderProductItem): DataResult<OrderProductItem>
    suspend fun updateItemQuantity(itemId: String, quantity: Int): DataResult<OrderProductItem>
}