package ru.takeshiko.matuleme.presentation.deliveryaddresslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.ItemAddressBinding
import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress

class AddressCardAdapter(
    private val addresses: List<UserDeliveryAddress>,
    private val onEditAddress: (UserDeliveryAddress) -> Unit = {},
) : RecyclerView.Adapter<AddressCardAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(
        private val binding: ItemAddressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(address: UserDeliveryAddress) {
            with (binding) {
                etAddress.setText(address.address)
                tilAddress.setEndIconOnClickListener { onEditAddress(address) }
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
}