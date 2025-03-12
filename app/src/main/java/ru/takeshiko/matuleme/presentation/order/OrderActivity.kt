package ru.takeshiko.matuleme.presentation.order

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivityOrderBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(SupabaseClientManager.getInstance())
    }

    private var currentOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            currentOrderId = intent.getStringExtra("order_id")!!

            viewModel.userOrderResult.observe(this@OrderActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val data = result.data
                        tvOrderNumber.text = getString(R.string.order_number, data.orderNumber)
                        itemCheckout.tvEmail.text = data.email
                        itemCheckout.tvPhone.text = data.phone
                        itemCheckout.etAddress.setText(data.address)
                        itemCheckout.etPaymentCard.setText(data.cardNumber)
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.userOrderProductItemsResult.observe(this@OrderActivity) { result ->
                when (result) {
                    is DataResult.Success -> {

                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrderDetails(currentOrderId!!)
    }
}