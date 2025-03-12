package ru.takeshiko.matuleme.presentation.checkout

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityCheckoutBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.createaddress.CreateAddressActivity
import ru.takeshiko.matuleme.presentation.createpaymentcard.CreatePaymentCardActivity
import ru.takeshiko.matuleme.presentation.main.MainActivity

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val viewModel: CheckoutViewModel by viewModels {
        CheckoutViewModelFactory(
            AppPreferencesManager.getInstance(),
            SupabaseClientManager.getInstance()
        )
    }
    private lateinit var toast: MaterialToast
    private var isProcessingOrder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@CheckoutActivity)

            itemCheckout.ivMap.setOnClickListener {
                val address = itemCheckout.etAddress.text.toString().trim()
                if (address.isNotEmpty()) {
                    openAddressInMaps(address)
                }
            }

            btnConfirm.setOnClickListener {
                if (!isProcessingOrder) {
                    isProcessingOrder = true
                    btnConfirm.isEnabled = false
                    viewModel.createOrder()
                }
            }

            viewModel.userResult.observe(this@CheckoutActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val userMetadata = result.data.userMetadata
                        if (userMetadata != null) {
                            itemCheckout.tvEmail.text = result.data.email

                            val phone = userMetadata["phone_number"]?.toString()?.trim('"') ?: "-"
                            itemCheckout.tvPhone.text = phone
                        }
                    }
                    is DataResult.Error -> {
                        toast.show(
                            getString(R.string.failed_title),
                            result.message,
                            R.drawable.ic_cross
                        )
                        Log.d(javaClass.name, result.message)
                    }
                }
            }

            viewModel.defaultAddress.observe(this@CheckoutActivity) { address ->
                if (address.isNullOrEmpty()) {
                    toast.show(
                        getString(R.string.no_address_found),
                        getString(R.string.add_address_message),
                        R.drawable.ic_cross,
                        onDismiss = {
                            startActivity(Intent(this@CheckoutActivity, CreateAddressActivity::class.java))
                        }
                    )
                } else {
                    itemCheckout.etAddress.setText(address)
                    checkPaymentCard()
                }
            }

            viewModel.subtotal.observe(this@CheckoutActivity) { subtotal ->
                tvSum.text = getString(R.string.price_format, subtotal)
            }

            viewModel.total.observe(this@CheckoutActivity) { total ->
                tvTotal.text = getString(R.string.price_format, total)
            }

            viewModel.orderCreationResult.observe(this@CheckoutActivity) { result ->
                isProcessingOrder = false
                btnConfirm.isEnabled = true

                when (result) {
                    is DataResult.Success -> {
                        toast.show(
                            getString(R.string.order_success_title),
                            getString(R.string.order_success_message),
                            R.drawable.ic_basket,
                            onDismiss = {
                                startActivity(Intent(this@CheckoutActivity, MainActivity::class.java))
                                finish()
                            }
                        )
                    }
                    is DataResult.Error -> {
                        toast.show(
                            getString(R.string.failed_title),
                            result.message,
                            R.drawable.ic_cross
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserData()
        viewModel.loadUserDefaultAddress()
        viewModel.loadUserDefaultPaymentCard()
        viewModel.loadCartItems()
    }

    private fun checkPaymentCard() {
        viewModel.defaultPaymentCard.observe(this@CheckoutActivity) { paymentCard ->
            if (paymentCard.isNullOrEmpty()) {
                toast.show(
                    getString(R.string.no_payment_card_found),
                    getString(R.string.add_payment_card_message),
                    R.drawable.ic_cross,
                    onDismiss = {
                        startActivity(Intent(this@CheckoutActivity, CreatePaymentCardActivity::class.java))
                    }
                )
            } else {
                binding.itemCheckout.etPaymentCard.setText(paymentCard)
                binding.itemCheckout.tilPaymentCard.startIconDrawable = ContextCompat.getDrawable(this@CheckoutActivity, R.drawable.ic_visa_card)
            }
        }
    }

    private fun openAddressInMaps(address: String) {
        val encodedAddress = java.net.URLEncoder.encode(address, "UTF-8")

        val googleMapsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://www.google.com/maps/search/?api=1&query=$encodedAddress".toUri()
        }

        val yandexMapsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "yandexmaps://maps.yandex.ru/?text=$encodedAddress".toUri()
        }

        val dgisIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "dgis://2gis.ru/query=$encodedAddress".toUri()
        }

        val chooser = Intent.createChooser(googleMapsIntent, getString(R.string.open_address_in)).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(yandexMapsIntent, dgisIntent))
        }

        try {
            startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            toast.show(
                getString(R.string.failed_title),
                getString(R.string.install_app_maps),
                R.drawable.ic_cross
            )
        }
    }
}