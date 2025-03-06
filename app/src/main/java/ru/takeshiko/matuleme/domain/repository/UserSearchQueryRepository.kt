package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.UserSearchQuery
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserSearchQueryRepository {
    suspend fun logQuery(query: UserSearchQuery): DataResult<UserSearchQuery>
    suspend fun getRecentQueriesByUser(userId: String, limit: Int): DataResult<List<UserSearchQuery>>
}