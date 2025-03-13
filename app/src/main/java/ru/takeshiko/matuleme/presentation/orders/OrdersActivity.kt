package ru.takeshiko.matuleme.presentation.orders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.OrderShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityOrdersBinding
import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.order.OrderActivity

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val viewModel: OrdersViewModel by viewModels {
        OrdersViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerAdapter: OrderShimmerAdapter
    private lateinit var adapter: OrderAdapter
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@OrdersActivity)

            shimmerAdapter = OrderShimmerAdapter(6)
            adapter = OrderAdapter(
                emptyList(),
                onClickListener = { order ->
                    startActivity(Intent(this@OrdersActivity, OrderActivity::class.java).apply {
                        putExtra("order_id", order.id)
                    })
                },
                onPayClickListener = { order ->
                    viewModel.updateOrderStatus(order, OrderStatus.PAID)
                    toast.show(
                        getString(R.string.successfully_paid_title),
                        getString(R.string.successfully_paid_message),
                        R.drawable.ic_checkmark
                    )
                }
            )

            rvOrders.apply {
                layoutManager = LinearLayoutManager(this@OrdersActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@OrdersActivity,
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@OrdersActivity,
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerAdapter
            }

            viewModel.userOrdersResult.observe(this@OrdersActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        adapter.updateList(result.data)
                        rvOrders.adapter = adapter
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserOrders()
    }
}