package ru.takeshiko.matuleme.presentation.checkout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.OrderProductItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.ProductRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserCartItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserDeliveryAddressRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserOrderRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserPaymentCardRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.OrderProductItem
import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard
import ru.takeshiko.matuleme.domain.models.result.DataResult

class CheckoutViewModel(
    private val appPreferencesManager: AppPreferencesManager,
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _cartItemsResult = MutableLiveData<DataResult<List<UserCartItem>>>()
    private val _productsResult = MutableLiveData<DataResult<List<Product>>>()

    private val _userResult = MutableLiveData<DataResult<UserInfo>>()
    val userResult: LiveData<DataResult<UserInfo>> = _userResult

    private val _defaultAddress = MutableLiveData<String>()
    val defaultAddress: LiveData<String> get() = _defaultAddress

    private val _defaultPaymentCard = MutableLiveData<String>()
    val defaultPaymentCard: LiveData<String> get() = _defaultPaymentCard

    private val _defaultAddressObject = MutableLiveData<UserDeliveryAddress?>()
    private val _defaultPaymentCardObject = MutableLiveData<UserPaymentCard?>()

    private val _orderCreationResult = MutableLiveData<DataResult<UserOrder>>()
    val orderCreationResult: LiveData<DataResult<UserOrder>> = _orderCreationResult

    private val userCartItemRepository = UserCartItemRepositoryImpl(supabaseClientManager)
    private val productRepository = ProductRepositoryImpl(supabaseClientManager)
    private val userDeliveryAddressRepository = UserDeliveryAddressRepositoryImpl(supabaseClientManager)
    private val userPaymentCardRepository = UserPaymentCardRepositoryImpl(supabaseClientManager)
    private val userOrderRepository = UserOrderRepositoryImpl(supabaseClientManager)
    private val orderProductItemRepository = OrderProductItemRepositoryImpl(supabaseClientManager)

    private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total

    private val deliveryCost = 0.0

    fun loadUserData() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                _userResult.value = DataResult.Success(user)
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun loadUserDefaultAddress() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                when (val result = userDeliveryAddressRepository.getAddressesByUserId(user.id)) {
                    is DataResult.Success -> {
                        val defaultAddressEntry = result.data.find { it.isDefault }
                        _defaultAddress.value = defaultAddressEntry?.address ?: ""
                        _defaultAddressObject.value = defaultAddressEntry
                    }
                    is DataResult.Error -> {
                        _defaultAddress.value = ""
                        _defaultAddressObject.value = null
                        Log.d(javaClass.name, result.message)
                    }
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun loadUserDefaultPaymentCard() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                when (val result = userPaymentCardRepository.getPaymentCardsByUserId(user.id)) {
                    is DataResult.Success -> {
                        val defaultPaymentCardEntry = result.data.find { it.isDefault }
                        _defaultPaymentCardObject.value = defaultPaymentCardEntry
                        _defaultPaymentCard.value = defaultPaymentCardEntry?.let { entry ->
                            val numberStr = entry.number.toString().padStart(16, '0')
                            "**** **** **** " + numberStr.takeLast(4)
                        } ?: ""
                    }
                    is DataResult.Error -> {
                        _defaultPaymentCard.value = ""
                        _defaultPaymentCardObject.value = null
                        Log.d(javaClass.name, result.message)
                    }
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

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

    fun createOrder() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                val cartItems = (_cartItemsResult.value as? DataResult.Success)?.data ?: run {
                    _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_empty_cart))
                    return@launch
                }
                val products = (_productsResult.value as? DataResult.Success)?.data ?: run {
                    _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_products_not_found))
                    return@launch
                }
                val paymentCard = _defaultPaymentCardObject.value ?: run {
                    _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_payment_card_not_selected))
                    return@launch
                }
                val address = _defaultAddressObject.value ?: run {
                    _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_delivery_address_not_selected))
                    return@launch
                }
                val totalAmount = _total.value ?: run {
                    _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_total_amount_not_calculated))
                    return@launch
                }

                val orderNumber = generateOrderNumber()

                val userOrder = UserOrder(
                    orderNumber = orderNumber,
                    userId = user.id,
                    email = user.email ?: "-",
                    phone = user.userMetadata?.get("phone_number")?.toString()?.trim('"') ?: "-",
                    cardNumber = paymentCard.number.toString().padStart(16, '0'),
                    cardHolder = paymentCard.holder,
                    address = address.address,
                    totalAmount = totalAmount,
                    status = OrderStatus.NEW,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )

                when (val orderResult = userOrderRepository.createOrder(userOrder)) {
                    is DataResult.Success -> {
                        val orderId = orderResult.data.id ?: run {
                            _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_order_id_not_generated))
                            return@launch
                        }

                        var allItemsAdded = true
                        for (cartItem in cartItems) {
                            val product = products.find { it.id == cartItem.productId } ?: continue

                            val orderItem = OrderProductItem(
                                orderId = orderId,
                                productId = cartItem.productId,
                                quantity = cartItem.quantity,
                                price = product.newPrice
                            )

                            when (val itemResult = orderProductItemRepository.addItem(orderItem)) {
                                is DataResult.Success -> {}
                                is DataResult.Error -> {
                                    Log.d(javaClass.name, itemResult.message)
                                    allItemsAdded = false
                                }
                            }
                        }

                        if (allItemsAdded) {
                            clearCart(user.id)
                            _orderCreationResult.value = DataResult.Success(orderResult.data)
                        } else {
                            userOrderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
                            _orderCreationResult.value = DataResult.Error(appPreferencesManager.getString(R.string.error_failed_to_add_order_items))
                        }
                    }
                    is DataResult.Error -> {
                        _orderCreationResult.value = DataResult.Error(appPreferencesManager.appContext.getString(R.string.error_failed_to_create_order, orderResult.message))
                        Log.d("LOG ORDER", orderResult.message)
                    }
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    private fun clearCart(userId: String) {
        viewModelScope.launch {
            when (val result = userCartItemRepository.removeAllByUserId(userId)) {
                is DataResult.Success -> Log.d(javaClass.name, "Cart for user $userId has been successfully truncated!")
                is DataResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        val random = (100000..999999).random().toString()
        return "$timestamp$random"
    }
}