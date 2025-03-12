package ru.takeshiko.matuleme.domain.models.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    @SerialName("new") NEW,
    @SerialName("paid") PAID,
    @SerialName("shipped") SHIPPED,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED
}