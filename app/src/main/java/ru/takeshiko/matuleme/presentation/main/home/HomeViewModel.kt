package ru.takeshiko.matuleme.presentation.main.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.storage.FileObject
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.ProductCategoryRepositoryImpl
import ru.takeshiko.matuleme.data.repository.ProductRepositoryImpl
import ru.takeshiko.matuleme.data.repository.StorageRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserCartItemRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserFavoriteRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.ProductCategory
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.database.UserFavorite
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.models.result.StorageResult

class HomeViewModel(
    private val appPreferencesManager: AppPreferencesManager,
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _productCategoryResult = MutableLiveData<DataResult<List<ProductCategory>>>()
    val productCategoryResult: LiveData<DataResult<List<ProductCategory>>> = _productCategoryResult

    private val _popularProductsResult = MutableLiveData<DataResult<List<Product>>>()
    val popularProductsResult: LiveData<DataResult<List<Product>>> = _popularProductsResult

    private val _favorites = MutableLiveData<Set<String>>(emptySet())
    val favorites: LiveData<Set<String>> = _favorites

    private val _cartItems = MutableLiveData<Set<String>>(emptySet())
    val cartItems: LiveData<Set<String>> = _cartItems

    private val _salesResult = MutableLiveData<StorageResult<List<FileObject>>>()
    val salesResult: LiveData<StorageResult<List<FileObject>>> = _salesResult

    private val storageRepository = StorageRepositoryImpl(supabaseClientManager)
    private val productCategoryRepository = ProductCategoryRepositoryImpl(supabaseClientManager)
    private val productRepository = ProductRepositoryImpl(supabaseClientManager)
    private val userFavoriteRepository = UserFavoriteRepositoryImpl(supabaseClientManager)
    private val userCartItemRepository = UserCartItemRepositoryImpl(supabaseClientManager)

    fun getAllProductCategories() {
        viewModelScope.launch {
            when (val result = productCategoryRepository.getAll()) {
                is DataResult.Success -> {
                    val allCategory = ProductCategory(
                        id = "all",
                        name = appPreferencesManager.getString(R.string.all_shoes),
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )
                    val categoriesWithAll = listOf(allCategory) + result.data
                    _productCategoryResult.value = DataResult.Success(categoriesWithAll)
                }
                is DataResult.Error -> _productCategoryResult.value = DataResult.Error(result.message)
            }
        }
    }

    fun getPopularProducts() {
        viewModelScope.launch {
            when (val userFavoritesResult = userFavoriteRepository.getAll()) {
                is DataResult.Success -> {
                    when (val allProductsResult = productRepository.getAll()) {
                        is DataResult.Success -> {
                            val favoriteCounts = userFavoritesResult.data
                                .groupingBy { it.productId }
                                .eachCount()

                            val sortedProducts = allProductsResult.data.sortedWith(
                                compareByDescending { favoriteCounts[it.id] ?: 0 }
                            )

                            val top5Products = sortedProducts.take(5)
                            _popularProductsResult.value = DataResult.Success(top5Products)
                        }
                        is DataResult.Error -> {
                            _popularProductsResult.value = DataResult.Error(allProductsResult.message)
                        }
                    }
                }
                is DataResult.Error -> {
                    _popularProductsResult.value = DataResult.Error(userFavoritesResult.message)
                }
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userFavoriteRepository.getByUserId(userId)) {
                    is DataResult.Success -> {
                        _favorites.value = result.data.map { it.productId }.toSet()
                    }
                    is DataResult.Error -> Log.e(javaClass.name, "Error loading favorites!")
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun loadCartItems() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userCartItemRepository.getByUserId(userId)) {
                    is DataResult.Success -> {
                        _cartItems.value = result.data.map { it.productId }.toSet()
                    }
                    is DataResult.Error -> Log.e(javaClass.name, "Error loading cart!")
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                if (_favorites.value?.contains(productId) == true) {
                    userFavoriteRepository.removeFavorite(userId, productId)
                } else {
                    userFavoriteRepository.addFavorite(
                        UserFavorite(
                            userId = userId,
                            productId = productId,
                            addedAt = Clock.System.now()
                        )
                    )
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
            loadFavorites()
        }
    }

    fun toggleCartItem(productId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                if (_cartItems.value?.contains(productId) == true) {
                    userCartItemRepository.removeCartItem(userId, productId)
                } else {
                    userCartItemRepository.addCartItem(
                        UserCartItem(
                            userId = userId,
                            productId = productId,
                            quantity = 1,
                            addedAt = Clock.System.now()
                        )
                    )
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
            loadCartItems()
        }
    }

    fun getSales() {
        viewModelScope.launch {
            when (val result = storageRepository.getAll("sales")) {
                is StorageResult.Success -> _salesResult.value = StorageResult.Success(result.data)
                is StorageResult.Error -> _salesResult.value = StorageResult.Error(result.message)
            }
        }
    }
}