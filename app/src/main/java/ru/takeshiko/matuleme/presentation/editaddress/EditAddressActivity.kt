package ru.takeshiko.matuleme.presentation.editaddress

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityEditAddressBinding

class EditAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAddressBinding
    private val viewModel: EditAddressViewModel by viewModels {
        EditAddressViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAddressBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@EditAddressActivity)

            val addressId = intent.getStringExtra("address_id")
            addressId?.let {
                viewModel.loadAddress(it)
            }

            btnSave.setOnClickListener {
                val newAddress = etAddress.text.toString().trim()

                if (addressId != null) {
                    if (newAddress.isNotEmpty()) {
                        viewModel.updateAddress(addressId, newAddress, viewModel.address.value!!.isDefault)
                        toast.show(
                            getString(R.string.successfully_save_address_title),
                            getString(R.string.successfully_save_address_message),
                            R.drawable.ic_checkmark,
                            onDismiss = { finish() }
                        )
                    } else {
                        toast.show(
                            getString(R.string.failed_save_address_title),
                            getString(R.string.failed_save_address_message),
                            R.drawable.ic_cross
                        )
                    }
                }
            }

            viewModel.address.observe(this@EditAddressActivity) { address ->
                address?.let {
                    etAddress.setText(it.address)
                }
            }
        }
    }
}