package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.UserNotification
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserNotificationRepository

class UserNotificationRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserNotificationRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getByUserId(userId: String): DataResult<List<UserNotification>> {
        return try {
            val result = postgrest
                .from("user_notifications")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserNotification>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user notifications!")
        }
    }

    override suspend fun getUnreadByUserId(userId: String): DataResult<List<UserNotification>> {
        return try {
            val result = postgrest
                .from("user_notifications")
                .select { filter { and { eq("user_id", userId); eq("is_read", false) } } }
                .decodeList<UserNotification>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user notifications!")
        }
    }

    override suspend fun markAsRead(notificationId: String): DataResult<UserNotification> {
        return try {
            val result = postgrest
                .from("user_notifications")
                .update({
                    set("is_read", true)
                }) { select(); filter { eq("id", notificationId) } }
                .decodeSingle<UserNotification>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to mark notification as read!")
        }
    }
}