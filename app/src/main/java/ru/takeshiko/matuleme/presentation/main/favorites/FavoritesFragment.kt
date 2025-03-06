package ru.takeshiko.matuleme.presentation.main.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.ProductCardShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.data.utils.setupAdaptiveGridLayout
import ru.takeshiko.matuleme.databinding.FragmentFavoritesBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.category.ProductCardAdapter
import ru.takeshiko.matuleme.presentation.product.ProductActivity

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var viewModel: FavoritesViewModel
    private lateinit var productAdapter: ProductCardAdapter
    private lateinit var shimmerAdapter: ProductCardShimmerAdapter
    private lateinit var toast: MaterialToast

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoritesBinding.bind(view)

        toast = MaterialToast(requireContext())

        val factory = FavoritesViewModelFactory(SupabaseClientManager.getInstance())
        viewModel = ViewModelProvider(this, factory)[FavoritesViewModel::class.java]

        with (binding) {
            shimmerAdapter = ProductCardShimmerAdapter(6)

            rvProducts.setupAdaptiveGridLayout(
                adapter = shimmerAdapter,
                cardWidthDp = 160,
                spacingDp = 16
            )

            productAdapter = ProductCardAdapter(
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

            viewModel.productsResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val currentFavorites = viewModel.favorites.value ?: emptySet()
                        val currentCartItems = viewModel.cartItems.value ?: emptySet()
                        productAdapter.updateData(
                            newProducts = result.data,
                            newFavorites = currentFavorites,
                            newCartItems = currentCartItems
                        )
                        rvProducts.adapter = productAdapter
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
                val currentProducts = (viewModel.productsResult.value as? DataResult.Success)?.data ?: emptyList()
                val filteredProducts = currentProducts.filter { favorites.contains(it.id) }
                productAdapter.updateData(
                    newProducts = filteredProducts,
                    newFavorites = favorites,
                    newCartItems = viewModel.cartItems.value ?: emptySet()
                )
            }

            viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
                val currentProducts = (viewModel.productsResult.value as? DataResult.Success)?.data ?: emptyList()
                val filteredByFavorites = currentProducts.filter { viewModel.favorites.value?.contains(it.id) == true }
                productAdapter.updateData(
                    newProducts = filteredByFavorites,
                    newFavorites = viewModel.favorites.value ?: emptySet(),
                    newCartItems = cartItems
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
        viewModel.loadCartItems()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}