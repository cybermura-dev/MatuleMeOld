package ru.takeshiko.matuleme.presentation.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.ProductCategoryRepositoryImpl
import ru.takeshiko.matuleme.data.repository.ProductRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserCartItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserFavoriteRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.ProductCategory
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.database.UserFavorite
import ru.takeshiko.matuleme.domain.models.result.DataResult

class ProductViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _productResult = MutableLiveData<DataResult<Product>>()
    val productResult: LiveData<DataResult<Product>> = _productResult

    private val _productCategoryResult = MutableLiveData<DataResult<ProductCategory>>()
    val productCategoryResult: LiveData<DataResult<ProductCategory>> = _productCategoryResult

    private val _isInCart = MutableLiveData<Boolean>()
    val isInCart: LiveData<Boolean> = _isInCart

    private val _isInFavorites = MutableLiveData<Boolean>()
    val isInFavorites: LiveData<Boolean> = _isInFavorites

    private val productRepository = ProductRepositoryImpl(supabaseClientManager)
    private val productCategoryRepository = ProductCategoryRepositoryImpl(supabaseClientManager)
    private val userFavoriteRepository = UserFavoriteRepositoryImpl(supabaseClientManager)
    private val userCartItemRepository = UserCartItemRepositoryImpl(supabaseClientManager)

    fun loadProductById(productId: String) {
        viewModelScope.launch {
            when (val result = productRepository.getProductById(productId)) {
                is DataResult.Success -> _productResult.value = DataResult.Success(result.data)
                is DataResult.Error -> _productResult.value = DataResult.Error(result.message)
            }
        }
    }

    fun loadCategoryById(categoryId: String) {
        viewModelScope.launch {
            when (val result = productCategoryRepository.getCategoryById(categoryId)) {
                is DataResult.Success -> _productCategoryResult.value = DataResult.Success(result.data)
                is DataResult.Error -> _productCategoryResult.value = DataResult.Error(result.message)
            }
        }
    }

    fun loadProductStatus(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (userCartItemRepository.get(userId, productId)) {
                    is DataResult.Success -> _isInCart.value = true
                    is DataResult.Error -> _isInCart.value = false
                }

                when (userFavoriteRepository.get(userId, productId)) {
                    is DataResult.Success -> _isInFavorites.value = true
                    is DataResult.Error -> _isInFavorites.value = false
                }
            } ?: run {
                _productResult.value = DataResult.Error("User not authenticated!")
            }
        }
    }

    fun addToFavorites(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                val favorite = UserFavorite(
                    userId = userId,
                    productId = productId,
                    addedAt = Clock.System.now()
                )
                when (val result = userFavoriteRepository.addFavorite(favorite)) {
                    is DataResult.Success -> _isInFavorites.value = true
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    fun removeFromFavorites(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userFavoriteRepository.removeFavorite(userId, productId)) {
                    is DataResult.Success -> _isInFavorites.value = false
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                val cartItem = UserCartItem(
                    userId = userId,
                    productId = productId,
                    quantity = 1,
                    addedAt = Clock.System.now()
                )
                when (val result = userCartItemRepository.addCartItem(cartItem)) {
                    is DataResult.Success -> _isInCart.value = true
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userCartItemRepository.removeCartItem(userId, productId)) {
                    is DataResult.Success -> _isInCart.value = false
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }
}