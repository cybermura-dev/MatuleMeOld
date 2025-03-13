package ru.takeshiko.matuleme.presentation.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.OrderProductItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.ProductRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserOrderRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.OrderProductItem
import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import ru.takeshiko.matuleme.domain.models.result.DataResult

class OrderViewModel(
    supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _userOrderResult = MutableLiveData<DataResult<UserOrder>>()
    val userOrderResult: LiveData<DataResult<UserOrder>> = _userOrderResult

    private val _userOrderProductItemsResult = MutableLiveData<DataResult<List<OrderProductItem>>>()
    val userOrderProductItemsResult: LiveData<DataResult<List<OrderProductItem>>> = _userOrderProductItemsResult

    private val _productsResult = MutableLiveData<DataResult<List<Product>>>()
    val productsResult: LiveData<DataResult<List<Product>>> = _productsResult

    private val userOrderRepository = UserOrderRepositoryImpl(supabaseClientManager)
    private val userOrderProductItemRepository = OrderProductItemRepositoryImpl(supabaseClientManager)
    private val productRepository = ProductRepositoryImpl(supabaseClientManager)

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
                    val productIds = result.data.map { it.productId }
                    loadProductsByIds(productIds)
                }
                is DataResult.Error -> _userOrderProductItemsResult.value = DataResult.Error(result.message)
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val currentOrder = (_userOrderResult.value as? DataResult.Success)?.data
            if (currentOrder != null && currentOrder.id == orderId) {
                currentOrder.status = status
                when (val result = userOrderRepository.updateOrder(orderId, currentOrder)) {
                    is DataResult.Success -> loadOrderDetails(orderId)
                    is DataResult.Error -> _userOrderResult.value = DataResult.Error(result.message)
                }
            }
        }
    }

    private fun loadProductsByIds(productIds: List<String>) {
        if (productIds.isEmpty()) {
            _productsResult.value = DataResult.Success(emptyList())
            return
        }

        viewModelScope.launch {
            when (val result = productRepository.getProductsByIds(productIds)) {
                is DataResult.Success -> {
                    _productsResult.value = DataResult.Success(result.data)
                }
                is DataResult.Error -> {
                    _productsResult.value = DataResult.Error(result.message)
                }
            }
        }
    }
}