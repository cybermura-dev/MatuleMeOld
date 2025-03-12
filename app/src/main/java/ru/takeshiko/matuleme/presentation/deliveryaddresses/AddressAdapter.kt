package ru.takeshiko.matuleme.presentation.deliveryaddresses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.ItemAddressBinding
import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress

class AddressAdapter(
    private var addresses: MutableList<UserDeliveryAddress> = mutableListOf(),
    private val onSetPrimaryClick: (UserDeliveryAddress) -> Unit,
    private val onEditClick: (UserDeliveryAddress) -> Unit,
    private val onDeleteClick: (UserDeliveryAddress) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(
        private val binding: ItemAddressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val swipeHandler: AddressSwipeToRevealHandler = AddressSwipeToRevealHandler(
            context = itemView.context,
            leftPanel = binding.primaryPanel,
            rightPanel = binding.actionsPanel,
            cardView = binding.addressCard
        ).apply { setupSwipeHandler() }

        init {
            binding.btnSetPrimary.setOnClickListener {
                val address = addresses[adapterPosition]
                onSetPrimaryClick(address)
                swipeHandler.resetSwipe()
            }

            binding.btnEdit.setOnClickListener {
                val address = addresses[adapterPosition]
                onEditClick(address)
                swipeHandler.resetSwipe()
            }

            binding.btnDelete.setOnClickListener {
                val address = addresses[adapterPosition]
                onDeleteClick(address)
                swipeHandler.resetSwipe()
            }
        }

        fun bind(address: UserDeliveryAddress) {
            with(binding) {
                tvAddress.text = address.address
                chipPrimaryAddress.visibility = if (address.isDefault) View.VISIBLE else View.GONE

                btnSetPrimary.isEnabled = !address.isDefault
                if (address.isDefault) {
                    btnSetPrimary.alpha = 0.5f
                } else {
                    btnSetPrimary.alpha = 1.0f
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addresses[position])
    }

    override fun getItemCount(): Int = addresses.size

    fun updateAddresses(newAddresses: List<UserDeliveryAddress>) {
        val diffCallback = AddressDiffCallback(addresses, newAddresses)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        addresses.clear()
        addresses.addAll(newAddresses)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeAddress(address: UserDeliveryAddress) {
        val index = addresses.indexOf(address)
        if (index != -1) {
            addresses.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class AddressDiffCallback(
        private val oldList: List<UserDeliveryAddress>,
        private val newList: List<UserDeliveryAddress>
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
