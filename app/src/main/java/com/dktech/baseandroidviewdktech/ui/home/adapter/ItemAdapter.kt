package com.dktech.baseandroidviewdktech.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dktech.baseandroidviewdktech.base.bottomsheet.adapter.MusicAdapter
import com.dktech.baseandroidviewdktech.databinding.ItemPaintMainBinding
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class ItemAdapter(
    val listItem: List<PaintingUIWrapper>,
    val onClick: (PaintingUIWrapper) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> {
                ItemViewHolder(ItemPaintMainBinding.inflate(layoutInflater))
            }

            2 -> {
                ItemLocalViewHolder(ItemPaintMainBinding.inflate(layoutInflater))
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
        if (listItem[position].cacheThumb == null) {
            (holder as ItemViewHolder).onBind()
        } else {
            (holder as ItemLocalViewHolder).onLocalBind()
        }
    }

    override fun getItemCount(): Int = listItem.size

    override fun getItemViewType(position: Int): Int =
        if (listItem[position].cacheThumb == null) {
            1 // This item hasn't beed downloaded
        } else {
            2 // Downloaded.
        }

    inner class ItemLocalViewHolder(
        val binding: ItemPaintMainBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onLocalBind() {
            val item = listItem[bindingAdapterPosition]
            binding.root.setSafeOnClickListener {
                onClick(item)
            }
        }
    }

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
                .load(item.remoteThumb)
                .into(binding.imageLine)
        }
    }
}
