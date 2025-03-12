package ru.takeshiko.matuleme.presentation.cart

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.ProductRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserCartItemRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.result.DataResult

class CartViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _cartItemsResult = MutableLiveData<DataResult<List<UserCartItem>>>()
    val cartItemsResult: LiveData<DataResult<List<UserCartItem>>> = _cartItemsResult

    private val _productsResult = MutableLiveData<DataResult<List<Product>>>()
    val productsResult: LiveData<DataResult<List<Product>>> = _productsResult

    private val userCartItemRepository = UserCartItemRepositoryImpl(supabaseClientManager)
    private val productRepository = ProductRepositoryImpl(supabaseClientManager)

    private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total

    private val deliveryCost = 0.0

    fun loadCartItems() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val cartItemsResult = userCartItemRepository.getByUserId(userId)) {
                    is DataResult.Success -> {
                        val productIds = cartItemsResult.data.map { it.productId }
                        when (val productsResult = productRepository.getProductsByIds(productIds)) {
                            is DataResult.Success -> {
                                _cartItemsResult.value = cartItemsResult
                                _productsResult.value = productsResult
                                calculateTotal()
                            }
                            is DataResult.Error -> _productsResult.value = DataResult.Error(productsResult.message)
                        }
                    }
                    is DataResult.Error -> _cartItemsResult.value = DataResult.Error(cartItemsResult.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun updateCartItem(item: UserCartItem) {
        viewModelScope.launch {
            when (val result = userCartItemRepository.updateQuantity(item.id!!, item.quantity)) {
                is DataResult.Success -> {
                    val currentCartItems = (_cartItemsResult.value as? DataResult.Success)?.data ?: emptyList()

                    val updatedCartItems = currentCartItems.map { cartItem ->
                        if (cartItem.id == item.id) item else cartItem
                    }

                    _cartItemsResult.value = DataResult.Success(updatedCartItems)
                    calculateTotal()
                }
                is DataResult.Error -> { _cartItemsResult.value = DataResult.Error(result.message) }
            }
        }
    }

    fun removeCartItem(item: UserCartItem) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userCartItemRepository.removeCartItem(userId, item.productId)) {
                    is DataResult.Success -> {
                        val updatedCartItems = (_cartItemsResult.value as? DataResult.Success)?.data
                            ?.filterNot { it.id == item.id } ?: emptyList()
                        _cartItemsResult.value = DataResult.Success(updatedCartItems)
                        calculateTotal()
                    }
                    is DataResult.Error -> {
                        _cartItemsResult.value = DataResult.Error(result.message)
                    }
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    private fun calculateTotal() {
        val cartItems = (_cartItemsResult.value as? DataResult.Success)?.data ?: return
        val products = (_productsResult.value as? DataResult.Success)?.data ?: return

        var subtotal = 0.0

        for (cartItem in cartItems) {
            val product = products.find { it.id == cartItem.productId }

            if (product != null) {
                subtotal += product.newPrice * cartItem.quantity
            }
        }

        val total = subtotal + deliveryCost

        _subtotal.value = subtotal
        _total.value = total
    }
}