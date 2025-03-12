package ru.takeshiko.matuleme.presentation.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.OrderProductItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserOrderRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.OrderProductItem
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import ru.takeshiko.matuleme.domain.models.result.DataResult

class OrderViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _userOrderResult = MutableLiveData<DataResult<UserOrder>>()
    val userOrderResult: LiveData<DataResult<UserOrder>> = _userOrderResult

    private val _userOrderProductItemsResult = MutableLiveData<DataResult<List<OrderProductItem>>>()
    val userOrderProductItemsResult: LiveData<DataResult<List<OrderProductItem>>> = _userOrderProductItemsResult

    private val userOrderRepository = UserOrderRepositoryImpl(supabaseClientManager)
    private val userOrderProductItemRepository = OrderProductItemRepositoryImpl(supabaseClientManager)

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            when (val result = userOrderRepository.getOrderById(orderId)) {
                is DataResult.Success -> {
                    _userOrderResult.value = DataResult.Success(result.data)
                    loadOrderItems(orderId)
                }
                is DataResult.Error -> _userOrderResult.value = DataResult.Error(result.message)
            }
        }
    }

    private fun loadOrderItems(orderId: String) {
        viewModelScope.launch {
            when (val result = userOrderProductItemRepository.getItemsByOrderId(orderId)) {
                is DataResult.Success -> {
                    _userOrderProductItemsResult.value = DataResult.Success(result.data)
                }
                is DataResult.Error -> _userOrderProductItemsResult.value = DataResult.Error(result.message)
            }
        }
    }
}