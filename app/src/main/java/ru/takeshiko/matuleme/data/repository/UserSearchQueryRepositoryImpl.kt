package ru.takeshiko.matuleme.data.repository

import io.github.jan.supabase.postgrest.query.Order
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.UserSearchQuery
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserSearchQueryRepository

class UserSearchQueryRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserSearchQueryRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun logQuery(query: UserSearchQuery): DataResult<UserSearchQuery> {
        return try {
            val result = postgrest
                .from("user_search_queries")
                .insert(query) { select() }
                .decodeSingle<UserSearchQuery>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to log query!")
        }
    }

    override suspend fun getRecentQueriesByUser(
        userId: String,
        limit: Int
    ): DataResult<List<UserSearchQuery>> {
        return try {
            val result = postgrest
                .from("user_search_queries")
                .select {
                    filter { eq("user_id", userId) }
                    order("searched_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<UserSearchQuery>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get recent queries by user!")
        }
    }
}