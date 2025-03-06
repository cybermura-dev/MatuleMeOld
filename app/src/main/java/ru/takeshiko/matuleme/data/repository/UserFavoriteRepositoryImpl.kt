package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.UserFavorite
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserFavoriteRepository

class UserFavoriteRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserFavoriteRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getAll(): DataResult<List<UserFavorite>> {
        return try {
            val result = postgrest.from("user_favorites")
                .select()
                .decodeList<UserFavorite>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user favorites!")
        }
    }

    override suspend fun get(userId: String, productId: String): DataResult<UserFavorite> {
        return try {
            val result = postgrest
                .from("user_favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("product_id", productId)
                    }
                }
                .decodeSingle<UserFavorite>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user favorite!")
        }
    }

    override suspend fun getByUserId(userId: String): DataResult<List<UserFavorite>> {
        return try {
            val result = postgrest
                .from("user_favorites")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserFavorite>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user favorites!")
        }
    }

    override suspend fun addFavorite(favorite: UserFavorite): DataResult<UserFavorite> {
        return try {
            val result = postgrest
                .from("user_favorites")
                .insert(favorite) { select() }
                .decodeSingle<UserFavorite>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to add favorite!")
        }
    }

    override suspend fun removeFavorite(userId: String, favoriteId: String) : DataResult<String> {
        return try {
            postgrest
                .from("user_favorites")
                .delete {
                    filter {
                        eq("product_id", favoriteId)
                        eq("user_id", userId)
                    }
                }
                .let {  }
            DataResult.Success(favoriteId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to remove favorite!")
        }
    }
}