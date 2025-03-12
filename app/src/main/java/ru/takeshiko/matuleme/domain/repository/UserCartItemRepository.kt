package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserCartItemRepository {
    suspend fun getByUserId(userId: String): DataResult<List<UserCartItem>>
    suspend fun get(userId: String, productId: String): DataResult<UserCartItem>
    suspend fun addCartItem(item: UserCartItem): DataResult<UserCartItem>
    suspend fun updateQuantity(itemId: String, newQuantity: Int): DataResult<UserCartItem>
    suspend fun removeCartItem(userId: String, itemId: String) : DataResult<String>
    suspend fun removeAllByUserId(userId: String) : DataResult<String>
}