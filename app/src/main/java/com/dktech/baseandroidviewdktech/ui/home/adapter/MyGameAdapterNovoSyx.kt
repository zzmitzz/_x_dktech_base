package com.dktech.baseandroidviewdktech.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bigbratan.emulair.common.metadata.retrograde.db.entity.Game
import com.bumptech.glide.RequestManager
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.databinding.ItemAdsBinding
import com.emulator.retro.console.game.databinding.ItemMyGameBinding
import com.emulator.retro.console.game.utils.gone
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class MyGameAdapterNovoSyx(
    private val mGlide: RequestManager?,
    private val onItemClick: (Game) -> Unit
): ListAdapter<Game, MyGameAdapterNovoSyx.MyGameViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.title == newItem.title && oldItem.coverFrontUrl == newItem.coverFrontUrl
            }
        }

        val itemAds by lazy {
            Game(-1, "", "", "", "", null, null, -1)
        }
    }

    inner class MyGameViewHolder(private val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(game: Game) {
            when  {
                binding is ItemMyGameBinding && game != itemAds -> bind(binding, game)
                binding is ItemAdsBinding && game == itemAds -> showAds(binding)
            }
        }

        private fun bind(binding: ItemMyGameBinding, game: Game) {
            mGlide?.load(game.coverFrontUrl)
                ?.placeholder(R.drawable.iv_load_error)
                ?.error(R.drawable.iv_load_error)
                ?.into(binding.iv) ?: binding.iv.setImageResource(R.drawable.iv_load_error)

            binding.textView.text = game.title
            itemView.setOnUnDoubleClick { onItemClick(game) }
        }

        private fun showAds(binding: ItemAdsBinding) {
            binding.frNative.gone()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isAds(position)) R.layout.item_my_game else R.layout.item_ads
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGameViewHolder {
        return MyGameViewHolder(
            if (viewType == R.layout.item_my_game) {
                ItemMyGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            } else {
                ItemAdsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        )
    }

    override fun onBindViewHolder(holder: MyGameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun isAds(position: Int) = currentList.getOrNull(position) == itemAds
}