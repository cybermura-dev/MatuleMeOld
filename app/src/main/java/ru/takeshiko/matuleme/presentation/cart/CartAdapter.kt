package ru.takeshiko.matuleme.presentation.cart

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.takeshiko.matuleme.BuildConfig
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.databinding.ItemCartBinding
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import java.util.Locale

class CartAdapter(
    private var products: List<Product> = emptyList(),
    private var cartItems: List<UserCartItem> = emptyList(),
    private val onPlusClick: (UserCartItem) -> Unit,
    private val onMinusClick: (UserCartItem) -> Unit,
    private val onDeleteClick: (UserCartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding)
        : RecyclerView.ViewHolder(binding.root) {

        private val swipeHandler: CartSwipeToRevealHandler = CartSwipeToRevealHandler(
            context = itemView.context,
            quantityPanel = itemView.findViewById(R.id.quantity_panel),
            deletePanel = itemView.findViewById(R.id.delete_panel),
            productCard = itemView.findViewById(R.id.cv_product)
        ).apply { setupSwipeHandler() }

        private var currentItem: UserCartItem? = null

        init {
            itemView.findViewById<ImageButton>(R.id.btn_plus).setOnClickListener {
                currentItem?.let {
                    onPlusClick(it)
                    swipeHandler.resetSwipe()
                }
            }

            itemView.findViewById<ImageButton>(R.id.btn_minus).setOnClickListener {
                currentItem?.let {
                    onMinusClick(it)
                    swipeHandler.resetSwipe()
                }
            }

            itemView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
                currentItem?.let {
                    onDeleteClick(it)
                    swipeHandler.resetSwipe()
                }
            }
        }

        fun bind(item: UserCartItem, product: Product) {
            currentItem = item

            with (binding) {
                tvTitle.text = product.title
                tvOldPrice.text = tvOldPrice.context.getString(
                    R.string.price_format,
                    product.oldPrice * item.quantity
                )
                tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvNewPrice.text = tvNewPrice.context.getString(
                    R.string.price_format,
                    product.newPrice * item.quantity
                )
                tvQuantity.text = String.format(Locale.getDefault(), "%d", item.quantity)

                val imageUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/products/${product.imageUrl}"
                Glide
                    .with(ivProduct.context)
                    .load(imageUrl)
                    .into(ivProduct)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        val product = products.find { it.id == item.productId } ?: return
        holder.bind(item, product)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newCartItems: List<UserCartItem>, newProducts: List<Product>) {
        val diffResult = DiffUtil.calculateDiff(CartDiffCallback(
            cartItems,
            newCartItems,
            products,
            newProducts
        ))

        cartItems = newCartItems
        products = newProducts
        diffResult.dispatchUpdatesTo(this)
    }

    private class CartDiffCallback(
        private val oldCartItems: List<UserCartItem>,
        private val newCartItems: List<UserCartItem>,
        private val oldProducts: List<Product>,
        private val newProducts: List<Product>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldCartItems.size
        override fun getNewListSize(): Int = newCartItems.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldCartItems[oldPos].id == newCartItems[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldCartItem = oldCartItems[oldPos]
            val newCartItem = newCartItems[newPos]

            val oldProduct = oldProducts.find { it.id == oldCartItem.productId }
            val newProduct = newProducts.find { it.id == newCartItem.productId }

            return oldCartItem.quantity == newCartItem.quantity &&
                    oldProduct?.title == newProduct?.title &&
                    oldProduct?.newPrice == newProduct?.newPrice
        }

        override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
            val oldCartItem = oldCartItems[oldPos]
            val newCartItem = newCartItems[newPos]

            val oldProduct = oldProducts.find { it.id == oldCartItem.productId }
            val newProduct = newProducts.find { it.id == newCartItem.productId }

            val quantityChanged = oldCartItem.quantity != newCartItem.quantity
            val priceChanged = oldProduct?.newPrice != newProduct?.newPrice

            return if (quantityChanged || priceChanged) {
                Bundle().apply {
                    putBoolean("quantityChanged", quantityChanged)
                    putBoolean("priceChanged", priceChanged)
                    putInt("newQuantity", newCartItem.quantity)
                    putDouble("newPrice", newProduct?.newPrice ?: 0.0)
                }
            } else null
        }
    }
}
