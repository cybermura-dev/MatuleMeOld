package ru.takeshiko.matuleme.presentation.paymentcards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.ItemPaymentCardBinding
import ru.takeshiko.matuleme.domain.models.database.UserPaymentCard

class PaymentCardAdapter(
    private var paymentCards: MutableList<UserPaymentCard> = mutableListOf(),
    private val onSetPrimaryClick: (UserPaymentCard) -> Unit,
    private val onEditClick: (UserPaymentCard) -> Unit,
    private val onDeleteClick: (UserPaymentCard) -> Unit
) : RecyclerView.Adapter<PaymentCardAdapter.PaymentCardViewHolder>() {

    inner class PaymentCardViewHolder(
        private val binding: ItemPaymentCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val swipeHandler: PaymentCardSwipeToRevealHandler = PaymentCardSwipeToRevealHandler(
            context = itemView.context,
            leftPanel = binding.primaryPanel,
            rightPanel = binding.actionsPanel,
            cardView = binding.cardPayment
        ).apply { setupSwipeHandler() }

        init {
            with (binding) {
                btnSetPrimary.setOnClickListener {
                    val paymentCard = paymentCards[adapterPosition]
                    onSetPrimaryClick(paymentCard)
                    swipeHandler.resetSwipe()
                }

                btnEdit.setOnClickListener {
                    val paymentCard = paymentCards[adapterPosition]
                    onEditClick(paymentCard)
                    swipeHandler.resetSwipe()
                }

                btnDelete.setOnClickListener {
                    val paymentCard = paymentCards[adapterPosition]
                    onDeleteClick(paymentCard)
                    swipeHandler.resetSwipe()
                }
            }
        }

        fun bind(paymentCard: UserPaymentCard) = with (binding) {
            val cardNumberStr = paymentCard.number.toString().padStart(16, '0')
            val maskedNumber = "**** **** **** " + cardNumberStr.takeLast(4)
            tvCardNumber.text = maskedNumber
            tvCardHolder.text = paymentCard.holder
            chipPrimaryCard.visibility = if (paymentCard.isDefault) View.VISIBLE else View.GONE

            btnSetPrimary.isEnabled = !paymentCard.isDefault
            if (paymentCard.isDefault) {
                btnSetPrimary.alpha = 0.5f
            } else {
                btnSetPrimary.alpha = 1.0f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentCardViewHolder {
        val binding = ItemPaymentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentCardViewHolder, position: Int) {
        holder.bind(paymentCards[position])
    }

    override fun getItemCount(): Int = paymentCards.size

    fun updatePaymentCards(newPaymentCards: List<UserPaymentCard>) {
        val diffCallback = PaymentCardDiffCallback(paymentCards, newPaymentCards)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        paymentCards.clear()
        paymentCards.addAll(newPaymentCards)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removePaymentCard(paymentCard: UserPaymentCard) {
        val index = paymentCards.indexOf(paymentCard)
        if (index != -1) {
            paymentCards.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class PaymentCardDiffCallback(
        private val oldList: List<UserPaymentCard>,
        private val newList: List<UserPaymentCard>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}