package ru.takeshiko.matuleme.presentation.main.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.CategoryShimmerAdapter
import ru.takeshiko.matuleme.data.adapters.ProductShimmerAdapter
import ru.takeshiko.matuleme.data.adapters.SaleShimmerAdapter
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.FragmentHomeBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.models.result.StorageResult
import ru.takeshiko.matuleme.presentation.category.CategoryActivity
import ru.takeshiko.matuleme.presentation.category.ProductAdapter
import ru.takeshiko.matuleme.presentation.category.ProductCategoryCardAdapter
import ru.takeshiko.matuleme.presentation.menu.MenuActivity
import ru.takeshiko.matuleme.presentation.product.ProductActivity
import ru.takeshiko.matuleme.presentation.search.SearchActivity

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var shimmerCategoryAdapter: CategoryShimmerAdapter
    private lateinit var shimmerPopularAdapter: ProductShimmerAdapter
    private lateinit var shimmerSaleAdapter: SaleShimmerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        val factory = HomeViewModelFactory(
            AppPreferencesManager.getInstance(),
            SupabaseClientManager.getInstance()
        )
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        with (binding) {
            shimmerCategoryAdapter = CategoryShimmerAdapter(6)
            shimmerPopularAdapter = ProductShimmerAdapter(2)
            shimmerSaleAdapter = SaleShimmerAdapter(2)

            ivHamburger.setOnClickListener {
                val intent = Intent(requireContext(), MenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }

            etSearch.setOnClickListener {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }

            rvCategories.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.HORIZONTAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.horizontal_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerCategoryAdapter
            }

            rvPopular.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.HORIZONTAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.horizontal_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerPopularAdapter
            }

            rvSales.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.HORIZONTAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.horizontal_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerSaleAdapter
            }

            viewModel.productCategoryResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val categories = result.data
                        rvCategories.adapter = ProductCategoryCardAdapter(categories) { category ->
                            val intent = Intent(requireContext(), CategoryActivity::class.java).apply {
                                putExtra("category_id", category.id)
                            }
                            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            startActivity(intent)
                        }
                    }

                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.popularProductsResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val popularProducts = result.data
                        rvPopular.adapter = ProductAdapter(
                            products = popularProducts,
                            favorites = viewModel.favorites.value ?: emptySet(),
                            cartItems = viewModel.cartItems.value ?: emptySet(),
                            onAddToFavoriteClick = { productId ->
                                viewModel.toggleFavorite(productId)
                            },
                            onAddToCartClick = { productId ->
                                viewModel.toggleCartItem(productId)
                            },
                            onOpenProductClick = { productId ->
                                startActivity(Intent(requireContext(), ProductActivity::class.java).apply {
                                    putExtra("product_id", productId)
                                })
                            }
                        )
                    }

                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
                (rvPopular.adapter as? ProductAdapter)?.updateFavorites(favorites)
            }

            viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
                (rvPopular.adapter as? ProductAdapter)?.updateCartItems(cartItems)
            }

            viewModel.salesResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is StorageResult.Success -> rvSales.adapter = SaleAdapter(result.data)
                    is StorageResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllProductCategories()
        viewModel.loadFavorites()
        viewModel.loadCartItems()
        viewModel.getPopularProducts()
        viewModel.getSales()
    }
}