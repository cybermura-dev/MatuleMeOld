package ru.takeshiko.matuleme.presentation.searchresult

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.data.adapters.ProductCardShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.setupAdaptiveGridLayout
import ru.takeshiko.matuleme.databinding.ActivitySearchResultBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.category.ProductCardAdapter
import ru.takeshiko.matuleme.presentation.product.ProductActivity
import ru.takeshiko.matuleme.presentation.search.SearchActivity

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private val viewModel: SearchResultViewModel by viewModels {
        SearchResultViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerProductAdapter: ProductCardShimmerAdapter
    private lateinit var query: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            query = intent.getStringExtra("query")!!

            shimmerProductAdapter = ProductCardShimmerAdapter(6)

            etSearch.setText(query)

            etSearch.setOnClickListener {
                startActivity(Intent(this@SearchResultActivity, SearchActivity::class.java))
                finish()
            }

            rvProducts.setupAdaptiveGridLayout(
                adapter = shimmerProductAdapter,
                cardWidthDp = 160,
                spacingDp = 16
            )

            val productCardAdapter = ProductCardAdapter(
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
                        rvProducts.adapter = productCardAdapter
                        viewModel.favorites.observe(this@SearchResultActivity) { favorites ->
                            productCardAdapter.updateFavorites(favorites)
                            viewModel.cartItems.observe(this@SearchResultActivity) { cartItems ->
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
        viewModel.getProductsByQuery(query)
        viewModel.loadFavorites()
        viewModel.loadCartItems()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}