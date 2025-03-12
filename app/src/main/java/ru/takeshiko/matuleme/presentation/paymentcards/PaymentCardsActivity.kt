package ru.takeshiko.matuleme.presentation.paymentcards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.PaymentCardShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivityPaymentCardsBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.createpaymentcard.CreatePaymentCardActivity
import ru.takeshiko.matuleme.presentation.editpaymentcard.EditPaymentCardActivity

class PaymentCardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentCardsBinding
    private val viewModel: PaymentCardsViewModel by viewModels {
        PaymentCardsViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var paymentCardAdapter: PaymentCardAdapter
    private lateinit var shimmerAdapter: PaymentCardShimmerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentCardsBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerAdapter = PaymentCardShimmerAdapter(6)
            paymentCardAdapter = PaymentCardAdapter(
                onEditClick = { paymentCard ->
                    startActivity(Intent(this@PaymentCardsActivity, EditPaymentCardActivity::class.java).apply {
                        putExtra("payment_card_id", paymentCard.id)
                    })
                },
                onDeleteClick = { paymentCard ->
                    viewModel.removePaymentCard(paymentCard)
                    paymentCardAdapter.removePaymentCard(paymentCard)
                },
                onSetPrimaryClick = { paymentCard ->
                    viewModel.setDefaultPaymentCard(paymentCard)
                }
            )

            btnAdd.setOnClickListener {
                startActivity(Intent(this@PaymentCardsActivity, CreatePaymentCardActivity::class.java))
            }

            rvPaymentCards.apply {
                layoutManager = LinearLayoutManager(this@PaymentCardsActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@PaymentCardsActivity,
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                this@PaymentCardsActivity,
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerAdapter
            }

            viewModel.paymentCardsResult.observe(this@PaymentCardsActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        paymentCardAdapter.updatePaymentCards(result.data)
                        rvPaymentCards.adapter = paymentCardAdapter
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserPaymentCards()
    }
}