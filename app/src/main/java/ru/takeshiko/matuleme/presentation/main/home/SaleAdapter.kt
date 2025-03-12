package ru.takeshiko.matuleme.presentation.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.jan.supabase.storage.FileObject
import ru.takeshiko.matuleme.BuildConfig
import ru.takeshiko.matuleme.databinding.ItemSaleBinding

class SaleAdapter(
    private val sales: List<FileObject>
) : RecyclerView.Adapter<SaleAdapter.SaleViewHolder>() {

    inner class SaleViewHolder(
        private val binding: ItemSaleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: FileObject) = with (binding) {
            val imageUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/sales/${sale.name}"
            Glide.with(ivSale.context)
                .load(imageUrl)
                .into(ivSale)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(sales[position])
    }

    override fun getItemCount(): Int = sales.size
}