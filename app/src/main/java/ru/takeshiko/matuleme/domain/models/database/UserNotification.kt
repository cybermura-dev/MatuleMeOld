package ru.takeshiko.matuleme.domain.models.database

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserNotification(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("title") val title: String,
    @SerialName("message") val message: String,
    @SerialName("type") val type: NotificationType,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: Instant
)