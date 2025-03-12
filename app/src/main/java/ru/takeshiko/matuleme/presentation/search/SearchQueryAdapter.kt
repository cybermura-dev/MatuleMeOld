package ru.takeshiko.matuleme.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.databinding.ItemSearchQueryBinding
import ru.takeshiko.matuleme.domain.models.database.UserSearchQuery

class SearchQueryAdapter(
    private val onItemClick: (UserSearchQuery) -> Unit,
    private val onDeleteClick: (UserSearchQuery) -> Unit
) : RecyclerView.Adapter<SearchQueryAdapter.SearchQueryViewHolder>() {

    private val items = mutableListOf<UserSearchQuery>()

    inner class SearchQueryViewHolder(private val binding: ItemSearchQueryBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserSearchQuery) = with (binding) {
            tvSearchQuery.text = item.query
            itemView.setOnClickListener { onItemClick(item) }
            ivDelete.setOnClickListener { onDeleteClick(item) }
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

    fun submitList(newItems: List<UserSearchQuery>) {
        val diffResult = DiffUtil.calculateDiff(SearchDiffCallback(items, newItems))

        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    private class SearchDiffCallback(
        private val oldList: List<UserSearchQuery>,
        private val newList: List<UserSearchQuery>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}