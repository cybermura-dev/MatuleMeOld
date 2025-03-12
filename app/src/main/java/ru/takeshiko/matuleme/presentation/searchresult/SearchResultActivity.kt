package ru.takeshiko.matuleme.presentation.searchresult

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.data.adapters.ProductShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.setupAdaptiveGridLayout
import ru.takeshiko.matuleme.databinding.ActivitySearchResultBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.category.ProductAdapter
import ru.takeshiko.matuleme.presentation.product.ProductActivity
import ru.takeshiko.matuleme.presentation.search.SearchActivity

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private val viewModel: SearchResultViewModel by viewModels {
        SearchResultViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerProductAdapter: ProductShimmerAdapter
    private lateinit var query: String
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            onBackPressedDispatcher.addCallback(this@SearchResultActivity , backCallback)

            query = intent.getStringExtra("query")!!

            shimmerProductAdapter = ProductShimmerAdapter(6)

            etSearch.setText(query)

            etSearch.setOnClickListener {
                startActivity(Intent(this@SearchResultActivity, SearchActivity::class.java))
                finish()
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
                    startActivity(Intent(this@SearchResultActivity, ProductActivity::class.java).apply {
                        putExtra("product_id", productId)
                    })
                }
            )

            viewModel.productsResult.observe(this@SearchResultActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val products = result.data
                        productCardAdapter.updateProducts(products)
                        viewModel.favorites.observe(this@SearchResultActivity) { favorites ->
                            productCardAdapter.updateFavorites(favorites)
                            viewModel.cartItems.observe(this@SearchResultActivity) { cartItems ->
                                productCardAdapter.updateCartItems(cartItems)
                            }
                        }
                        rvProducts.adapter = productCardAdapter
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getProductsByQuery(query)
        viewModel.loadFavorites()
        viewModel.loadCartItems()
    }
}