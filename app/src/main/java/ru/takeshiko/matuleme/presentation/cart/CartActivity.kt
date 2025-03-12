package ru.takeshiko.matuleme.presentation.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.CartShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityCartBinding
import ru.takeshiko.matuleme.domain.models.database.UserCartItem
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.checkout.CheckoutActivity

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: CartViewModel by viewModels {
        CartViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerCartAdapter: CartShimmerAdapter
    private lateinit var cartAdapter: CartAdapter
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerCartAdapter = CartShimmerAdapter(6)

            toast = MaterialToast(this@CartActivity)

            cartAdapter = CartAdapter(
                onPlusClick = { item -> onPlusClicked(item) },
                onMinusClick = { item -> onMinusClicked(item) },
                onDeleteClick = { item -> onDeleteClicked(item) }
            )

            btnCheckout.setOnClickListener {
                startActivity(Intent(this@CartActivity, CheckoutActivity::class.java))
            }

            rvCartItems.apply {
                layoutManager = LinearLayoutManager(this@CartActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@CartActivity,
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@CartActivity,
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerCartAdapter
            }

            viewModel.cartItemsResult.observe(this@CartActivity) { cartResult ->
                when (cartResult) {
                    is DataResult.Success -> {
                        viewModel.productsResult.observe(this@CartActivity) { productsResult ->
                            when (productsResult) {
                                is DataResult.Success -> {
                                    rvCartItems.adapter = shimmerCartAdapter
                                    cartAdapter.updateData(cartResult.data, productsResult.data)
                                    rvCartItems.adapter = cartAdapter
                                    updateCheckoutButtonState(cartResult.data)
                                }
                                is DataResult.Error -> Log.d(javaClass.name, productsResult.message)
                            }
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, cartResult.message)
                }
            }

            viewModel.subtotal.observe(this@CartActivity) { subtotal ->
                tvSum.text = getString(R.string.price_format, subtotal)
            }

            viewModel.total.observe(this@CartActivity) { total ->
               tvTotal.text = getString(R.string.price_format, total)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCartItems()
    }

    private fun updateCheckoutButtonState(cartItems: List<UserCartItem>) {
        with (binding) {
            if (cartItems.isEmpty()) {
                btnCheckout.isEnabled = false
                btnCheckout.alpha = 0.5f
            } else {
                btnCheckout.isEnabled = true
                btnCheckout.alpha = 1f
            }
        }
    }

    private fun onPlusClicked(item: UserCartItem) {
        if (item.quantity <= 99) {
            val updatedItem = item.copy(quantity = item.quantity + 1)
            viewModel.updateCartItem(updatedItem)
        }
    }

    private fun onMinusClicked(item: UserCartItem) {
        if (item.quantity > 1) {
            val updatedItem = item.copy(quantity = item.quantity - 1)
            viewModel.updateCartItem(updatedItem)
        }
    }

    private fun onDeleteClicked(item: UserCartItem) {
        viewModel.removeCartItem(item)
    }
}

