package ru.takeshiko.matuleme.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.LayoutSearchQueryCardShimmerBinding

class SearchQueryCardShimmerAdapter(private val itemCount: Int = 5)
    : RecyclerView.Adapter<SearchQueryCardShimmerAdapter.ShimmerViewHolder>() {

    class ShimmerViewHolder(binding: LayoutSearchQueryCardShimmerBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val binding = LayoutSearchQueryCardShimmerBinding.inflate(
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