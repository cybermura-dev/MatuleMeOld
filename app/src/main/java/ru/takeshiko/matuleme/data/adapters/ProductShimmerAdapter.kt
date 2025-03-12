package ru.takeshiko.matuleme.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.LayoutProductCardShimmerBinding

class ProductShimmerAdapter(private val itemCount: Int = 6)
    : RecyclerView.Adapter<ProductShimmerAdapter.ShimmerViewHolder>() {

    class ShimmerViewHolder(binding: LayoutProductCardShimmerBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val binding = LayoutProductCardShimmerBinding.inflate(
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