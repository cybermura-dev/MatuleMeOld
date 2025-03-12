package ru.takeshiko.matuleme.domain.models.database

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserOrder(
    @SerialName("id") val id: String? = null,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("user_id") val userId: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("card_number") val cardNumber: String,
    @SerialName("card_holder") val cardHolder: String,
    @SerialName("address") val address: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("status") val status: OrderStatus,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant
)