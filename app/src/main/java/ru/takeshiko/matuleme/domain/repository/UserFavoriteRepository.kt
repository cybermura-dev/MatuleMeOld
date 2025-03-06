package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.UserFavorite
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserFavoriteRepository {
    suspend fun getAll(): DataResult<List<UserFavorite>>
    suspend fun get(userId: String, productId: String): DataResult<UserFavorite>
    suspend fun getByUserId(userId: String): DataResult<List<UserFavorite>>
    suspend fun addFavorite(favorite: UserFavorite): DataResult<UserFavorite>
    suspend fun removeFavorite(userId: String, favoriteId: String) : DataResult<String>
}