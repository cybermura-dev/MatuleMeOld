package ru.takeshiko.matuleme.presentation.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserCartItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserNotificationRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.database.UserNotification
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserCartItemRepository
import ru.takeshiko.matuleme.domain.repository.UserNotificationRepository

class MainViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _cartItemsResult = MutableLiveData<DataResult<List<UserCartItem>>>()
    val cartItemsResult: LiveData<DataResult<List<UserCartItem>>> = _cartItemsResult

    private val _unreadNotificationsResult = MutableLiveData<DataResult<List<UserNotification>>>()
    val unreadNotificationsResult: LiveData<DataResult<List<UserNotification>>> = _unreadNotificationsResult

    private val userCartItemRepository: UserCartItemRepository = UserCartItemRepositoryImpl(supabaseClientManager)
    private val userNotificationRepository: UserNotificationRepository = UserNotificationRepositoryImpl(supabaseClientManager)

    private fun loadCartItems() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userCartItemRepository.getByUserId(userId)) {
                    is DataResult.Success -> _cartItemsResult.value = DataResult.Success(result.data)
                    is DataResult.Error -> _cartItemsResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    private fun loadUnreadNotifications() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userNotificationRepository.getUnreadByUserId(userId)) {
                    is DataResult.Success -> _unreadNotificationsResult.value = DataResult.Success(result.data)
                    is DataResult.Error -> _unreadNotificationsResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun startNotificationsPolling() {
        viewModelScope.launch {
            while (isActive) {
                if (supabaseClientManager.auth.currentSessionOrNull() != null) {
                    loadUnreadNotifications()
                    loadCartItems()
                    delay(10_000)
                }
            }
        }
    }
}