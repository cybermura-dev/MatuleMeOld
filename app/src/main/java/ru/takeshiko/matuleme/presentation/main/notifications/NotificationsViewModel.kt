package ru.takeshiko.matuleme.presentation.main.notifications

import android.util.Log
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
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userNotificationRepository.getByUserId(userId)) {
                    is DataResult.Success -> {
                        val sortedNotifications = result.data
                            .sortedWith(
                                compareBy<UserNotification> { it.isRead }
                                    .thenByDescending { it.createdAt }
                            )
                        _notificationsResult.value = DataResult.Success(sortedNotifications)
                    }
                    is DataResult.Error -> _notificationsResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }


    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (val result = userNotificationRepository.markAsRead(notificationId)) {
                is DataResult.Success -> {
                    _notificationsResult.value = _notificationsResult.value?.let { dataResult ->
                        if (dataResult is DataResult.Success) {
                            val updatedList = dataResult.data.map {
                                if (it.id == notificationId) it.copy(isRead = true) else it
                            }.sortedWith(
                                compareBy<UserNotification> { it.isRead }
                                    .thenByDescending { it.createdAt }
                            )
                            DataResult.Success(updatedList)
                        } else {
                            dataResult
                        }
                    }
                }
                is DataResult.Error -> _notificationsResult.value = DataResult.Error(result.message)
            }
        }
    }
}