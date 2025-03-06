package ru.takeshiko.matuleme.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.ItemSearchQueryBinding
import ru.takeshiko.matuleme.domain.models.database.UserSearchQuery

class SearchQueryCardAdapter(
    private val items: List<UserSearchQuery>,
    private val onItemClick: (UserSearchQuery) -> Unit
) : RecyclerView.Adapter<SearchQueryCardAdapter.SearchQueryViewHolder>() {

    inner class SearchQueryViewHolder(private val binding: ItemSearchQueryBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserSearchQuery) {
            with (binding) {
                tvSearchQuery.text = item.query
                itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchQueryViewHolder {
        val binding = ItemSearchQueryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchQueryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchQueryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}