package ru.takeshiko.matuleme.presentation.category

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.takeshiko.matuleme.BuildConfig
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.databinding.ItemProductBinding
import ru.takeshiko.matuleme.domain.models.database.Product

class ProductAdapter(
    var products: List<Product> = emptyList(),
    var favorites: Set<String> = emptySet(),
    var cartItems: Set<String> = emptySet(),
    private val onAddToFavoriteClick: (String) -> Unit = {},
    private val onAddToCartClick: (String) -> Unit = {},
    private val onOpenProductClick: (String) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) = with (binding) {
            tvTitle.text = product.title
            tvOldPrice.text = tvOldPrice.context.getString(
                R.string.price_format,
                product.oldPrice
            )
            tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvNewPrice.text = tvNewPrice.context.getString(
                R.string.price_format,
                product.newPrice
            )

            val imageUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/products/${product.imageUrl}"
            Glide
                .with(ivProduct.context)
                .load(imageUrl)
                .error(R.drawable.highlight_02)
                .into(ivProduct)

            updateIcons(favorites.contains(product.id), cartItems.contains(product.id))

            btnAddToFavorite.setOnClickListener { onAddToFavoriteClick(product.id!!) }
            ivAddToCart.setOnClickListener { onAddToCartClick(product.id!!) }
            cvProduct.setOnClickListener { onOpenProductClick(product.id!!) }
        }

        fun updateIcons(isFavorite: Boolean, isInCart: Boolean) {
            with (binding) {
                btnAddToFavorite.setImageResource(
                    if (isFavorite) R.drawable.ic_favorite_fill else R.drawable.ic_favorite
                )

                btnAddToFavorite.setColorFilter(
                    ContextCompat.getColor(
                        itemView.context,
                        if (isFavorite) R.color.error_color else R.color.black
                    )
                )

                btnAddToCart.setImageResource(
                    if (isInCart) R.drawable.ic_checkmark else R.drawable.ic_add
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            payloads.forEach { payload ->
                when (payload) {
                    is Bundle -> {
                        val isFavorite = payload.getBoolean("isFavorite")
                        val isInCart = payload.getBoolean("isInCart")
                        holder.updateIcons(isFavorite, isInCart)
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        val diffResult = DiffUtil.calculateDiff(
            ProductDiffCallback(
                products,
                newProducts,
                favorites,
                favorites,
                cartItems,
                cartItems
            )
        )
        products = newProducts
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateFavorites(newFavorites: Set<String>) {
        val diffResult = DiffUtil.calculateDiff(
            ProductDiffCallback(
                products,
                products,
                favorites,
                newFavorites,
                cartItems,
                cartItems
            )
        )
        favorites = newFavorites
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateCartItems(newCartItems: Set<String>) {
        val diffResult = DiffUtil.calculateDiff(
            ProductDiffCallback(
                products,
                products,
                favorites,
                favorites,
                cartItems,
                newCartItems
            )
        )
        cartItems = newCartItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateData(
        newProducts: List<Product>,
        newFavorites: Set<String>,
        newCartItems: Set<String>
    ) {
        val diffResult = DiffUtil.calculateDiff(ProductDiffCallback(
            products,
            newProducts,
            favorites,
            newFavorites,
            cartItems,
            newCartItems
        ))

        products = newProducts.toMutableList()
        favorites = newFavorites.toMutableSet()
        cartItems = newCartItems.toMutableSet()

        diffResult.dispatchUpdatesTo(this)
    }

    private class ProductDiffCallback(
        private val oldProducts: List<Product>,
        private val newProducts: List<Product>,
        private val oldFavorites: Set<String>,
        private val newFavorites: Set<String>,
        private val oldCartItems: Set<String>,
        private val newCartItems: Set<String>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldProducts.size
        override fun getNewListSize(): Int = newProducts.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldProducts[oldPos].id == newProducts[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldItem = oldProducts[oldPos]
            val newItem = newProducts[newPos]

            return oldItem.id == newItem.id
                    && oldItem.title == newItem.title
                    && oldItem.newPrice == newItem.newPrice
                    && oldFavorites.contains(oldItem.id) == newFavorites.contains(newItem.id)
                    && oldCartItems.contains(oldItem.id) == newCartItems.contains(newItem.id)
        }

        override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
            val oldItem = oldProducts[oldPos]
            val newItem = newProducts[newPos]
            val favoriteChanged = oldFavorites.contains(oldItem.id) != newFavorites.contains(newItem.id)
            val cartChanged = oldCartItems.contains(oldItem.id) != newCartItems.contains(newItem.id)
            return if (favoriteChanged || cartChanged) {
                Bundle().apply {
                    putBoolean("favoriteChanged", favoriteChanged)
                    putBoolean("cartChanged", cartChanged)
                    putBoolean("isFavorite", newFavorites.contains(newItem.id))
                    putBoolean("isInCart", newCartItems.contains(newItem.id))
                }
            } else {
                null
            }
        }
    }
}
