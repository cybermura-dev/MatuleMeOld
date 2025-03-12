package ru.takeshiko.matuleme.presentation.createpaymentcard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityCreatePaymentCardBinding

class CreatePaymentCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePaymentCardBinding
    private val viewModel: CreatePaymentCardViewModel by viewModels {
        CreatePaymentCardViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePaymentCardBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@CreatePaymentCardActivity)

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

                if (newNumber.isNotEmpty() && newHolder.isNotEmpty()) {
                    viewModel.savePaymentCard(newNumber, newHolder)
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