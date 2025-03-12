package ru.takeshiko.matuleme.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.LayoutOrderCardShimmerBinding

class OrderShimmerAdapter(private val itemCount: Int = 6)
    : RecyclerView.Adapter<OrderShimmerAdapter.ShimmerViewHolder>() {

    class ShimmerViewHolder(binding: LayoutOrderCardShimmerBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val binding = LayoutOrderCardShimmerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShimmerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = itemCount
}