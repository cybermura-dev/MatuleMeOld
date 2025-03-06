package ru.takeshiko.matuleme.presentation.main.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserNotificationRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserNotification
import ru.takeshiko.matuleme.domain.models.result.DataResult

class NotificationsViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _notificationsResult = MutableLiveData<DataResult<List<UserNotification>>>()
    val notificationsResult: LiveData<DataResult<List<UserNotification>>> = _notificationsResult

    private val userNotificationRepository = UserNotificationRepositoryImpl(supabaseClientManager)

    fun getAllNotifications() {
        viewModelScope.launch {
            val user = supabaseClientManager.auth.currentUserOrNull()
            if (user != null) {
                when (val result = userNotificationRepository.getByUserId(user.id)) {
                    is DataResult.Success -> {
                        val sortedNotifications = result.data.sortedByDescending { it.createdAt }
                        _notificationsResult.value = DataResult.Success(sortedNotifications)
                    }
                    is DataResult.Error -> _notificationsResult.value = DataResult.Error(result.message)
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (val result = userNotificationRepository.markAsRead(notificationId)) {
                is DataResult.Success -> getAllNotifications()
                is DataResult.Error -> _notificationsResult.value = DataResult.Error(result.message)
            }
        }
    }
}