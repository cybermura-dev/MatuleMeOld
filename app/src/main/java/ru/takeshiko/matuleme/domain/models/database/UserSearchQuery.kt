package ru.takeshiko.matuleme.domain.models.database

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSearchQuery(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("query") val query: String,
    @SerialName("searched_at") val searchedAt: Instant
)