package ru.takeshiko.matuleme.presentation.order

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.CartShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityOrderBinding
import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.result.DataResult
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast
    private lateinit var shimmerAdapter: CartShimmerAdapter
    private lateinit var adapter: OrderProductAdapter

    private var currentOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@OrderActivity)

            shimmerAdapter = CartShimmerAdapter(6)
            adapter = OrderProductAdapter()

            currentOrderId = intent.getStringExtra("order_id")!!

            btnCancel.setOnClickListener {
                viewModel.updateOrderStatus(currentOrderId!!, OrderStatus.CANCELLED)
                toast.show(
                    getString(R.string.successfully_cancelled_order_title),
                    getString(R.string.successfully_cancelled_order_message),
                    R.drawable.ic_checkmark,
                    onDismiss = { finish() }
                )
            }

            btnConfirm.setOnClickListener {
                viewModel.updateOrderStatus(currentOrderId!!, OrderStatus.COMPLETED)
                toast.show(
                    getString(R.string.successfully_confirmed_order_title),
                    getString(R.string.successfully_confirmed_order_message),
                    R.drawable.ic_checkmark,
                    onDismiss = { finish() }
                )
            }

            rvOrderItems.apply {
                layoutManager = LinearLayoutManager(this@OrderActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@OrderActivity,
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@OrderActivity,
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerAdapter
            }

            viewModel.userOrderResult.observe(this@OrderActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val data = result.data

                        tvOrderNumber.text = getString(R.string.order_number, data.orderNumber)
                        tvOrderStatus.text = getStatusText(data.status)
                        tvOrderStatus.setTextColor(
                            when (data.status) {
                                OrderStatus.COMPLETED -> ContextCompat.getColor(this@OrderActivity, R.color.success_color)
                                OrderStatus.CANCELLED -> ContextCompat.getColor(this@OrderActivity, R.color.error_color)
                                else -> ContextCompat.getColor(this@OrderActivity, R.color.accent_color)
                            }
                        )

                        with (itemCheckout) {
                            tvEmail.text = data.email
                            tvPhone.text = data.phone
                            etAddress.setText(data.address)

                            val numberStr = data.cardNumber.toString().padStart(16, '0').takeLast(4)
                            etPaymentCard.setText(String.format(Locale.ROOT, "**** **** **** $numberStr"))
                            tilPaymentCard.startIconDrawable = ContextCompat.getDrawable(this@OrderActivity, R.drawable.ic_visa_card)
                        }

                        btnCancel.visibility = if (data.status !in setOf(OrderStatus.CANCELLED, OrderStatus.COMPLETED, OrderStatus.SHIPPED)) View.VISIBLE else View.GONE
                        btnConfirm.visibility = if (data.status == OrderStatus.SHIPPED) View.VISIBLE else View.GONE
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.userOrderProductItemsResult.observe(this@OrderActivity) { orderItemsResult ->
                when (orderItemsResult) {
                    is DataResult.Success -> {
                        val orderItems = orderItemsResult.data

                        val productsResult = viewModel.productsResult.value
                        if (productsResult is DataResult.Success) {
                            adapter.updateData(orderItems, productsResult.data)
                            rvOrderItems.adapter = adapter
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, orderItemsResult.message)
                }
            }

            viewModel.productsResult.observe(this@OrderActivity) { productsResult ->
                if (productsResult is DataResult.Success) {
                    val products = productsResult.data

                    val orderItemsResult = viewModel.userOrderProductItemsResult.value
                    if (orderItemsResult is DataResult.Success) {
                        adapter.updateData(orderItemsResult.data, products)
                        rvOrderItems.adapter = adapter
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrderDetails(currentOrderId!!)
    }

    private fun getStatusText(status: OrderStatus): String = with (binding.tvOrderStatus.context) {
        return when (status) {
            OrderStatus.NEW -> getString(R.string.status_new)
            OrderStatus.PAID -> getString(R.string.status_paid)
            OrderStatus.SHIPPED -> getString(R.string.status_shipped)
            OrderStatus.COMPLETED -> getString(R.string.status_completed)
            OrderStatus.CANCELLED -> getString(R.string.status_cancelled)
        }
    }
}