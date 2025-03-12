package ru.takeshiko.matuleme.presentation.product

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import ru.takeshiko.matuleme.BuildConfig
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivityProductBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerFrameContainer: ShimmerFrameLayout
    private lateinit var productId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerFrameContainer = shimmerContainer.root
            shimmerFrameContainer.startShimmer()

            productId = intent.getStringExtra("product_id")!!

            btnAddToFavorite.setOnClickListener {
                viewModel.isInFavorites.value?.let { isInFavorites ->
                    if (isInFavorites) {
                        viewModel.removeFromFavorites(productId)
                    } else {
                        viewModel.addToFavorites(productId)
                    }
                }
            }

            btnAddToCart.setOnClickListener {
                viewModel.isInCart.value?.let { isInCart ->
                    if (isInCart) {
                        viewModel.removeFromCart(productId)
                    } else {
                        viewModel.addToCart(productId)
                    }
                }
            }

            viewModel.productResult.observe(this@ProductActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val product = result.data
                        viewModel.loadCategoryById(product.categoryId!!)

                        tvTitle.text = product.title
                        tvPrice.text = tvPrice.context.getString(
                            R.string.price_format,
                            product.newPrice
                        )
                        tvDescription.text = product.description

                        val imageUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/products/${product.imageUrl}"
                        Glide.with(ivImage.context)
                            .load(imageUrl)
                            .into(ivImage)

                        shimmerFrameContainer.stopShimmer()
                        shimmerFrameContainer.visibility = View.GONE
                        cvProduct.visibility = View.VISIBLE
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.productCategoryResult.observe(this@ProductActivity) { result ->
                when (result) {
                    is DataResult.Success -> tvCategory.text = result.data.name
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.isInCart.observe(this@ProductActivity) { isInCart ->
                val color = if (isInCart) {
                    ContextCompat.getColor(this@ProductActivity, R.color.error_color)
                } else {
                    ContextCompat.getColor(this@ProductActivity, R.color.accent_color)
                }

                btnAddToCart.backgroundTintList = ColorStateList.valueOf(color)
                btnAddToCart.setText(
                    if (isInCart) R.string.added_to_cart else R.string.add_to_cart
                )
            }

            viewModel.isInFavorites.observe(this@ProductActivity) { isInFavorites ->
                ivFavorite.setImageResource(
                    if (isInFavorites) R.drawable.ic_favorite_fill else R.drawable.ic_favorite
                )

                ivFavorite.setColorFilter(
                    ContextCompat.getColor(
                        this@ProductActivity,
                        if (isInFavorites) R.color.error_color else R.color.black
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProductById(productId)
        viewModel.loadProductStatus(productId)
    }

    override fun onDestroy() {
        shimmerFrameContainer.stopShimmer()
        super.onDestroy()
    }
}