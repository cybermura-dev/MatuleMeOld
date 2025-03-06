package ru.takeshiko.matuleme.presentation.deliveryaddresslist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.AddressCardShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivityDeliveryAddressListBinding

class DeliveryAddressListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryAddressListBinding
    private val viewModel: DeliveryAddressListViewModel by viewModels {
        DeliveryAddressListViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerAdapter: AddressCardShimmerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryAddressListBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerAdapter = AddressCardShimmerAdapter(6)

            rvAddresses.apply {
                layoutManager = LinearLayoutManager(this@DeliveryAddressListActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@DeliveryAddressListActivity,
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@DeliveryAddressListActivity,
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerAdapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}