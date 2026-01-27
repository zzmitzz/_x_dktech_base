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
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class ItemAdapter(
    private val onClick: (PaintingUIWrapper) -> Unit,
) : ListAdapter<PaintingUIWrapper, RecyclerView.ViewHolder>(PaintingDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPaintMainBinding.inflate(layoutInflater, parent, false)
        return when (viewType) {
            VIEW_TYPE_REMOTE -> ItemViewHolder(binding)
            VIEW_TYPE_LOCAL -> ItemLocalViewHolder(binding)
            else -> ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        when (holder) {
            is ItemLocalViewHolder -> holder.bind(item)
            is ItemViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int = if (getItem(position).cacheThumb == null) VIEW_TYPE_REMOTE else VIEW_TYPE_LOCAL

    inner class ItemLocalViewHolder(
        private val binding: ItemPaintMainBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaintingUIWrapper) {
            Glide
                .with(binding.root)
                .load(item.cacheThumb)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imageLine)

            binding.root.setSafeOnClickListener {
                onClick(item)
            }
        }
    }

    inner class ItemViewHolder(
        private val binding: ItemPaintMainBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaintingUIWrapper) {
            Glide
                .with(binding.root)
                .load(item.remoteThumb)
                .into(binding.imageLine)
            binding.root.setSafeOnClickListener {
                onClick(item)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_REMOTE = 1
        private const val VIEW_TYPE_LOCAL = 2
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
