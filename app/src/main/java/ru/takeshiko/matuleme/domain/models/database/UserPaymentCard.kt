package ru.takeshiko.matuleme.domain.models.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPaymentCard(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("number") val number: String,
    @SerialName("holder") val holder: String,
    @SerialName("is_default") val isDefault: Boolean
)