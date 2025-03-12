package ru.takeshiko.matuleme.presentation.deliveryaddresses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.AddressShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivityDeliveryAddressesBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.createaddress.CreateAddressActivity
import ru.takeshiko.matuleme.presentation.editaddress.EditAddressActivity

class DeliveryAddressesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryAddressesBinding
    private val viewModel: DeliveryAddressesViewModel by viewModels {
        DeliveryAddressesViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var shimmerAdapter: AddressShimmerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryAddressesBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerAdapter = AddressShimmerAdapter(6)
            addressAdapter = AddressAdapter(
                onEditClick = { address ->
                    startActivity(Intent(this@DeliveryAddressesActivity, EditAddressActivity::class.java).apply {
                        putExtra("address_id", address.id)
                    })
                },
                onDeleteClick = { address ->
                    viewModel.removeAddress(address)
                    addressAdapter.removeAddress(address)
                },
                onSetPrimaryClick = { address ->
                    viewModel.setDefaultAddress(address)
                }
            )

            btnAdd.setOnClickListener {
                startActivity(Intent(this@DeliveryAddressesActivity, CreateAddressActivity::class.java))
            }

            rvAddresses.apply {
                layoutManager = LinearLayoutManager(this@DeliveryAddressesActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@DeliveryAddressesActivity,
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@DeliveryAddressesActivity,
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerAdapter
            }

            viewModel.addressesResult.observe(this@DeliveryAddressesActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        addressAdapter.updateAddresses(result.data)
                        rvAddresses.adapter = addressAdapter
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserAddresses()
    }
}