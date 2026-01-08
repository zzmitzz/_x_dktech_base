package com.dktech.baseandroidviewdktech.ui.home.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.data.remote.models.RetrixData
import com.emulator.retro.console.game.databinding.ItemAdsBinding
import com.emulator.retro.console.game.databinding.ItemHomeGameBinding
import com.emulator.retro.console.game.utils.gone
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class HomeGameAdapterNovoSyx(
    private val mGlide: RequestManager,
    private val onItemClick: (RetrixData) -> Unit
): ListAdapter<RetrixData, HomeGameAdapterNovoSyx.HomeGameViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<RetrixData>() {
            override fun areItemsTheSame(oldItem: RetrixData, newItem: RetrixData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RetrixData, newItem: RetrixData): Boolean {
                return oldItem.title == newItem.title && oldItem.thumb == newItem.thumb && oldItem.downloadLink == newItem.downloadLink
            }
        }

        val itemAds = RetrixData(-1, "", "", "", "", "", "", "", "")
    }

    inner class HomeGameViewHolder(private val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RetrixData) {
            when  {
                binding is ItemHomeGameBinding && data != itemAds -> bind(binding, data)
                binding is ItemAdsBinding && data == itemAds -> showAds(binding)
            }
        }

        private fun bind(binding: ItemHomeGameBinding, data: RetrixData) {
            binding.tvName.text = Html.fromHtml(data.title, Html.FROM_HTML_MODE_LEGACY)
            mGlide.load(data.thumb)
                .placeholder(R.drawable.iv_load_error)
                .error(R.drawable.iv_load_error)
                .into(binding.iv)

            itemView.setOnUnDoubleClick {
                onItemClick(data)
            }
        }

        private fun showAds(binding: ItemAdsBinding) {
            binding.frNative.gone()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isAds(position)) R.layout.item_home_game else R.layout.item_ads
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGameViewHolder {
        return HomeGameViewHolder(
            if (viewType == R.layout.item_home_game) {
                ItemHomeGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            } else {
                ItemAdsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        )
    }

    override fun onBindViewHolder(holder: HomeGameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun isAds(position: Int) = currentList.getOrNull(position) == itemAds
}