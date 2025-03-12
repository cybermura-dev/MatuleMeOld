package ru.takeshiko.matuleme.presentation.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.databinding.ItemCategoryBinding
import ru.takeshiko.matuleme.domain.models.database.ProductCategory

class ProductCategoryCardAdapter(
    private val items: List<ProductCategory>,
    private val onItemClick: (ProductCategory) -> Unit
) : RecyclerView.Adapter<ProductCategoryCardAdapter.ProductCategoryViewHolder>() {

    private var selectedCategoryId: String? = null

    inner class ProductCategoryViewHolder(private val binding: ItemCategoryBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProductCategory) = with (binding) {
            tvCategory.text = item.name

            val context = itemView.context
            val selectedColor = ContextCompat.getColor(context, R.color.accent_color)
            val defaultColor = ContextCompat.getColor(context, R.color.white)
            val selectedTextColor = ContextCompat.getColor(context, R.color.white)
            val defaultTextColor = ContextCompat.getColor(context, R.color.black)

            if (item.id == selectedCategoryId) {
                cvCategory.setCardBackgroundColor(selectedColor)
                tvCategory.setTextColor(selectedTextColor)
            } else {
                cvCategory.setCardBackgroundColor(defaultColor)
                tvCategory.setTextColor(defaultTextColor)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductCategoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setSelectedCategoryId(categoryId: String?) {
        val oldSelectedCategoryId = this.selectedCategoryId
        this.selectedCategoryId = categoryId

        if (oldSelectedCategoryId != categoryId) {
            val oldPosition = items.indexOfFirst { it.id == oldSelectedCategoryId }
            val newPosition = items.indexOfFirst { it.id == categoryId }

            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }
    }
}