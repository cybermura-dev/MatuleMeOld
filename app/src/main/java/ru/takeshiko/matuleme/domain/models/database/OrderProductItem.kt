package ru.takeshiko.matuleme.domain.models.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderProductItem(
    @SerialName("id") val id: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price") val price: Double
)