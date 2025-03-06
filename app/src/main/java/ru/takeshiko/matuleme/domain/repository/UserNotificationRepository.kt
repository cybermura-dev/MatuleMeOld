package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.UserNotification
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserNotificationRepository {
    suspend fun getByUserId(userId: String): DataResult<List<UserNotification>>
    suspend fun getUnreadByUserId(userId: String): DataResult<List<UserNotification>>
    suspend fun markAsRead(notificationId: String): DataResult<UserNotification>
}