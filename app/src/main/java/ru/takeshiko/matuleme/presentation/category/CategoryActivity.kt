package ru.takeshiko.matuleme.presentation.category

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.CategoryShimmerAdapter
import ru.takeshiko.matuleme.data.adapters.ProductShimmerAdapter
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.setupAdaptiveGridLayout
import ru.takeshiko.matuleme.databinding.ActivityCategoryBinding
import ru.takeshiko.matuleme.domain.models.database.ProductCategory
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.product.ProductActivity

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private val viewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(
            AppPreferencesManager.getInstance(),
            SupabaseClientManager.getInstance()
        )
    }
    private lateinit var shimmerCategoryAdapter: CategoryShimmerAdapter
    private lateinit var shimmerProductAdapter: ProductShimmerAdapter

    private var selectedCategoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerCategoryAdapter = CategoryShimmerAdapter(6)
            shimmerProductAdapter = ProductShimmerAdapter(6)

            val categoryId = intent.getStringExtra("category_id")!!
            selectedCategoryId = categoryId

            rvCategories.apply {
                layoutManager = LinearLayoutManager(
                    this@CategoryActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                addItemDecoration(
                    DividerItemDecoration(this@CategoryActivity, DividerItemDecoration.HORIZONTAL).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@CategoryActivity,
                                R.drawable.horizontal_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerCategoryAdapter
            }

            rvProducts.apply {
                setupAdaptiveGridLayout(
                    adapter = shimmerProductAdapter,
                    cardWidthDp = 160,
                    spacingDp = 16
                )
            }

            val productCardAdapter = ProductAdapter(
                onAddToFavoriteClick = { productId ->
                    viewModel.toggleFavorite(productId)
                },
                onAddToCartClick = { productId ->
                    viewModel.toggleCartItem(productId)
                },
                onOpenProductClick = { productId ->
                    startActivity(Intent(this@CategoryActivity, ProductActivity::class.java).apply {
                        putExtra("product_id", productId)
                    })
                }
            )

            viewModel.productCategoryResult.observe(this@CategoryActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val categories = result.data
                        val adapter = ProductCategoryCardAdapter(categories) { category ->
                            selectCategory(category)
                        }
                        rvCategories.adapter = adapter
                        adapter.setSelectedCategoryId(selectedCategoryId)

                        val selectedCategory = categories.find { it.id == selectedCategoryId }
                        tvCategoryName.text = selectedCategory?.name ?: ""
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.productsResult.observe(this@CategoryActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val products = result.data
                        productCardAdapter.updateProducts(products)
                        rvProducts.adapter = productCardAdapter
                        viewModel.favorites.observe(this@CategoryActivity) { favorites ->
                            productCardAdapter.updateFavorites(favorites)
                            viewModel.cartItems.observe(this@CategoryActivity) { cartItems ->
                                productCardAdapter.updateCartItems(cartItems)
                            }
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllProductCategories()
        viewModel.getProductsByCategory(selectedCategoryId!!)
        viewModel.loadFavorites()
        viewModel.loadCartItems()
    }

    private fun selectCategory(category: ProductCategory) {
        with (binding) {
            selectedCategoryId = category.id
            tvCategoryName.text = category.name
            (rvCategories.adapter as ProductCategoryCardAdapter).setSelectedCategoryId(selectedCategoryId)
            rvProducts.adapter = shimmerProductAdapter

            viewModel.getProductsByCategory(category.id!!)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}