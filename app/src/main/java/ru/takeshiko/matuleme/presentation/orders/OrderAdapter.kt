package ru.takeshiko.matuleme.presentation.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.databinding.ItemOrderBinding
import ru.takeshiko.matuleme.domain.models.database.OrderStatus
import ru.takeshiko.matuleme.domain.models.database.UserOrder
import java.util.Locale

class OrderAdapter(
    private var items: List<UserOrder>,
    private val onClickListener: (UserOrder) -> Unit,
    private val onPayClickListener: (UserOrder) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: UserOrder) = with (binding) {
            tvOrderNumber.text = root.context.getString(R.string.order_number, order.orderNumber)
            tvOrderDate.text = formatInstant(order.createdAt)
            tvTotalAmount.text = tvTotalAmount.context.getString(R.string.price_format, order.totalAmount)

            with (binding.flOrderStatus) {
                background = when (order.status) {
                    OrderStatus.CANCELLED -> ContextCompat.getDrawable(context, R.drawable.bg_status_cancelled)
                    OrderStatus.COMPLETED -> ContextCompat.getDrawable(context, R.drawable.bg_status_completed)
                    else -> ContextCompat.getDrawable(context, R.drawable.bg_status_accent)
                }
            }

            tvOrderStatus.text = getStatusText(order.status)
            updateProgress(order.status)

            btnPay.visibility = if (order.status == OrderStatus.NEW) {
                btnPay.setOnClickListener { onPayClickListener(order) }
                View.VISIBLE
            } else {
                View.GONE
            }

            root.setOnClickListener { onClickListener(order) }
        }

        private fun getStatusText(status: OrderStatus): String = with (binding.tvOrderStatus.context) {
            return when (status) {
                OrderStatus.NEW -> getString(R.string.status_new)
                OrderStatus.PAID -> getString(R.string.status_paid)
                OrderStatus.SHIPPED -> getString(R.string.status_shipped)
                OrderStatus.COMPLETED -> getString(R.string.status_completed)
                OrderStatus.CANCELLED -> getString(R.string.status_cancelled)
            }
        }

        private fun updateProgress(status: OrderStatus) = with (binding) {
            val progress = when (status) {
                OrderStatus.CANCELLED -> 0
                OrderStatus.NEW -> 25
                OrderStatus.PAID -> 50
                OrderStatus.SHIPPED -> 75
                OrderStatus.COMPLETED -> 100
            }
            progressIndicator.progress = progress

            with (root.context) {
                progressIndicator.setIndicatorColor(
                    when (status) {
                        OrderStatus.CANCELLED -> ContextCompat.getColor(this, R.color.error_color)
                        OrderStatus.COMPLETED -> ContextCompat.getColor(this, R.color.success_color)
                        else -> ContextCompat.getColor(this, R.color.accent_color)
                    }
                )
                tvProgressPercent.setTextColor(
                    when (status) {
                        OrderStatus.CANCELLED -> ContextCompat.getColor(this, R.color.error_color)
                        OrderStatus.COMPLETED -> ContextCompat.getColor(this, R.color.success_color)
                        else -> ContextCompat.getColor(this, R.color.accent_color)
                    }
                )
                tvProgressPercent.text = getString(R.string.progress_percent, progress)
            }
        }

        private fun formatInstant(instant: Instant): String {
            val now = Clock.System.now()
            val duration = now - instant

            with (binding.tvOrderDate.context) {
                return when {
                    duration.inWholeSeconds < 60 -> "${duration.inWholeSeconds} ${getString(R.string.seconds_ago)}"
                    duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes} ${getString(R.string.minutes_ago)}"
                    duration.inWholeHours < 24 -> "${duration.inWholeHours} ${getString(R.string.hours_ago)}"
                    duration.inWholeDays < 7 -> "${duration.inWholeDays} ${getString(R.string.days_ago)}"
                    else -> {
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        String.format(
                            Locale.getDefault(),
                            "%02d.%02d.%04d, %02d:%02d",
                            localDateTime.dayOfMonth,
                            localDateTime.monthNumber,
                            localDateTime.year,
                            localDateTime.hour,
                            localDateTime.minute
                        )
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<UserOrder>) {
        val diffCallback = OrderDiffCallback(items, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class OrderDiffCallback(
        private val oldList: List<UserOrder>,
        private val newList: List<UserOrder>
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
