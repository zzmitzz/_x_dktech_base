package com.emulator.retro.console.game.retro_game.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import com.bumptech.glide.signature.ObjectKey
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.databinding.ItemSaveStateSlotBinding
import com.emulator.retro.console.game.databinding.ItemSavedStateBinding
import com.emulator.retro.console.game.retro_game.manager.state.models.SaveStateModelNovoSyx
import com.emulator.retro.console.game.utils.formatDateTime
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class SaveStateAdapterNovoSyx(
    private val mGlide: RequestManager,
    private val onItemClick: (Int, Boolean) -> Unit
) : ListAdapter<SaveStateModelNovoSyx, SaveStateAdapterNovoSyx.SaveStateViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SaveStateModelNovoSyx>() {
            override fun areItemsTheSame(
                oldItem: SaveStateModelNovoSyx,
                newItem: SaveStateModelNovoSyx
            ): Boolean {
                return oldItem.mDateTime == newItem.mDateTime
            }

            override fun areContentsTheSame(
                oldItem: SaveStateModelNovoSyx,
                newItem: SaveStateModelNovoSyx
            ): Boolean {
                return oldItem.mTitle == newItem.mTitle && oldItem.mPreview == newItem.mPreview
            }
        }
    }

    private val mPlaceholder by lazy { Color.BLACK.toDrawable() }

    inner class SaveStateViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val mContext by lazy { binding.root.context }
        fun bind(model: SaveStateModelNovoSyx) {
            when {
                binding is ItemSavedStateBinding && model != SaveStateModelNovoSyx.none -> bind(binding, model)
                binding is ItemSaveStateSlotBinding && model == SaveStateModelNovoSyx.none -> bind(binding)
            }
        }

        private fun bind(binding: ItemSavedStateBinding, model: SaveStateModelNovoSyx) {
            binding.tvName.text = model.mTitle
            binding.tvDateTime.text = model.mDateTime.formatDateTime("dd/MM/yyyy, hh:mm")
            mGlide.load(model.mPreview)
                .placeholder(mPlaceholder)
                .error(R.drawable.iv_load_error)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.iv)

            itemView.setOnUnDoubleClick { onItemClick(absoluteAdapterPosition, true) }
        }

        private fun bind(binding: ItemSaveStateSlotBinding) {
            binding.tv.text = mContext.getString(R.string.slot, absoluteAdapterPosition + 1)

            itemView.setOnUnDoubleClick { onItemClick(absoluteAdapterPosition, false) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position] == SaveStateModelNovoSyx.none) {
            R.layout.item_save_state_slot
        } else {
            R.layout.item_saved_state
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveStateViewHolder {
        return SaveStateViewHolder(
            if (viewType == R.layout.item_save_state_slot) {
                ItemSaveStateSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            } else {
                ItemSavedStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        )
    }

    override fun onBindViewHolder(holder: SaveStateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}