package com.dktech.baseandroidviewdktech.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dktech.baseandroidviewdktech.databinding.ItemPaintMainBinding
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.CustomLoadingImage
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class ItemAdapter(
    private val onClick: (PaintingUIWrapper) -> Unit,
) : ListAdapter<PaintingUIWrapper, ItemAdapter.ItemViewHolder>(PaintingDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPaintMainBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemPaintMainBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaintingUIWrapper) {
            CustomLoadingImage.loadImage(
                item,
                binding.imageLine,
                binding.llShimmer,
                binding.underLayer
            )
            binding.root.setSafeOnClickListener {
                onClick(item)
            }
        }
    }
}

class PaintingDiffCallback : DiffUtil.ItemCallback<PaintingUIWrapper>() {
    override fun areItemsTheSame(
        oldItem: PaintingUIWrapper,
        newItem: PaintingUIWrapper,
    ): Boolean = oldItem.fileName == newItem.fileName

    override fun areContentsTheSame(
        oldItem: PaintingUIWrapper,
        newItem: PaintingUIWrapper,
    ): Boolean = oldItem == newItem
}
