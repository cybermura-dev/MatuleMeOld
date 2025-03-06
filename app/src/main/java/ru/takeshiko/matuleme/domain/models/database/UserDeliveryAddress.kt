package ru.takeshiko.matuleme.domain.models.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDeliveryAddress(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("address") val address: String,
    @SerialName("is_default") val isDefault: Boolean
)