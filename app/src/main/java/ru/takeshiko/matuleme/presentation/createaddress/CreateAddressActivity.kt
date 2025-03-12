package ru.takeshiko.matuleme.presentation.createaddress

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityCreateAddressBinding

class CreateAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAddressBinding
    private val viewModel: CreateAddressViewModel by viewModels {
        CreateAddressViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAddressBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@CreateAddressActivity)

            btnSave.setOnClickListener {
                val address = etAddress.text.toString().trim()

                if (address.isNotEmpty()) {
                    viewModel.saveAddress(address)
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
    }
}