package ru.takeshiko.matuleme.domain.models.database

import kotlinx.serialization.SerialName

enum class NotificationType {
    @SerialName("order_update") ORDER_UPDATE,
    @SerialName("discount") DISCOUNT,
    @SerialName("message") MESSAGE,
    @SerialName("info") INFO
}