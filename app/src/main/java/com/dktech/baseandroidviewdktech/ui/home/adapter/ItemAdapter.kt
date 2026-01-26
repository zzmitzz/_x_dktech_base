package com.dktech.baseandroidviewdktech.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dktech.baseandroidviewdktech.databinding.ItemPaintMainBinding
import com.dktech.baseandroidviewdktech.utils.Painting
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class ItemAdapter(
    val listItem: List<Painting>,
    val onClick: (Painting) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> {
                ItemViewHolder(ItemPaintMainBinding.inflate(layoutInflater))
            }

            2 -> {
                ItemViewHolder(ItemPaintMainBinding.inflate(layoutInflater))
            }

            else -> {
                ItemViewHolder(ItemPaintMainBinding.inflate(layoutInflater))
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (listItem[position].fileName == null) {
            (holder as ItemViewHolder).onBind()
        } else {
            (holder as ItemViewHolder).onBind()

        }
    }

    override fun getItemCount(): Int = listItem.size

    override fun getItemViewType(position: Int): Int {
        if (listItem[position].fileName == null) {
            return 1 // This item hasn't beed downloaded
        } else {
            return 2 // Downloaded.
        }
    }

    inner class ItemLocalViewHolder

    inner class ItemViewHolder(
        val binding: ItemPaintMainBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val item = listItem[bindingAdapterPosition]
            binding.root.setSafeOnClickListener {
                onClick(item)
            }
            Glide
                .with(binding.root)
                .load(item.imageThumbRemote)
                .into(binding.imageLine)
        }
    }
}
