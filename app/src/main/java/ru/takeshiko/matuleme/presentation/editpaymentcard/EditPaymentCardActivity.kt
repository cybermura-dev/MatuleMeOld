package ru.takeshiko.matuleme.presentation.editpaymentcard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityEditPaymentCardBinding

class EditPaymentCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPaymentCardBinding
    private val viewModel: EditPaymentCardViewModel by viewModels {
        EditPaymentCardViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPaymentCardBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@EditPaymentCardActivity)

            val paymentCardId = intent.getStringExtra("payment_card_id")
            paymentCardId?.let {
                viewModel.loadPaymentCard(it)
            }

            btnSave.setOnClickListener {
                val formattedNumber = etPaymentCardNumber.text.toString().trim()
                val newNumber = formattedNumber.replace("-", "")
                val newHolder = etPaymentCardHolder.text.toString().trim()

                if (newNumber.length != 16) {
                    toast.show(
                        getString(R.string.invalid_card_number_title),
                        getString(R.string.invalid_card_number_message),
                        R.drawable.ic_cross
                    )
                    return@setOnClickListener
                }

                if (paymentCardId != null) {
                    if (newNumber.isNotEmpty() && newHolder.isNotEmpty()) {
                        viewModel.updatePaymentCard(
                            paymentCardId,
                            newNumber,
                            newHolder,
                            viewModel.paymentCard.value!!.isDefault
                        )
                        toast.show(
                            getString(R.string.successfully_save_payment_card_title),
                            getString(R.string.successfully_save_payment_card_message),
                            R.drawable.ic_checkmark,
                            onDismiss = { finish() }
                        )
                    } else {
                        toast.show(
                            getString(R.string.failed_save_payment_card_title),
                            getString(R.string.failed_save_payment_card_message),
                            R.drawable.ic_cross
                        )
                    }
                }
            }

            viewModel.paymentCard.observe(this@EditPaymentCardActivity) { paymentCard ->
                paymentCard?.let {
                    etPaymentCardNumber.setText(it.number)
                    etPaymentCardHolder.setText(it.holder)
                }
            }

            etPaymentCardNumber.addTextChangedListener(object : TextWatcher {
                private var isEditing = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isEditing || s.isNullOrEmpty()) return

                    isEditing = true

                    val cleanText = s.toString().replace(Regex("\\D"), "")

                    val trimmedText = if (cleanText.length > 16) cleanText.substring(0, 16) else cleanText

                    val formatted = StringBuilder()
                    for (i in trimmedText.indices) {
                        formatted.append(trimmedText[i])
                        if ((i + 1) % 4 == 0 && i + 1 != trimmedText.length) {
                            formatted.append("-")
                        }
                    }

                    etPaymentCardNumber.setText(formatted.toString())
                    etPaymentCardNumber.setSelection(formatted.length)

                    isEditing = false
                }
            })
        }
    }
}