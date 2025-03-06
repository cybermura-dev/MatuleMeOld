package ru.takeshiko.matuleme.domain.models.database

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("category_id") val categoryId: String?,
    @SerialName("old_price") val oldPrice: Double,
    @SerialName("new_price") val newPrice: Double,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant
)