package ru.takeshiko.matuleme.presentation.orders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserOrderRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard
import ru.takeshiko.matuleme.domain.models.result.DataResult

class OrdersViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _userOrdersResult = MutableLiveData<DataResult<List<UserOrder>>>()
    val userOrdersResult: LiveData<DataResult<List<UserOrder>>> = _userOrdersResult

    private val userOrderRepository = UserOrderRepositoryImpl(supabaseClientManager)

    fun loadUserOrders() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userOrderRepository.getOrdersByUserId(userId)) {
                    is DataResult.Success -> _userOrdersResult.value = DataResult.Success(result.data)
                    is DataResult.Error -> _userOrdersResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}