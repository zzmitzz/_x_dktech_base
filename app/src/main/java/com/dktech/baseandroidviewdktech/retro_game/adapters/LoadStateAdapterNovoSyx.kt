package com.emulator.retro.console.game.retro_game.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.signature.ObjectKey
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.databinding.ItemSavedStateBinding
import com.emulator.retro.console.game.retro_game.manager.state.models.LoadStateModelNovoSyx
import com.emulator.retro.console.game.utils.formatDateTime
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class LoadStateAdapterNovoSyx(
    private val mGlide: RequestManager,
    private val onItemClick: (Int) -> Unit
) : ListAdapter<LoadStateModelNovoSyx, LoadStateAdapterNovoSyx.LoadStateViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LoadStateModelNovoSyx>() {
            override fun areItemsTheSame(
                oldItem: LoadStateModelNovoSyx,
                newItem: LoadStateModelNovoSyx
            ): Boolean {
                return oldItem.iSlot == newItem.iSlot
            }

            override fun areContentsTheSame(
                oldItem: LoadStateModelNovoSyx,
                newItem: LoadStateModelNovoSyx
            ): Boolean {
                return oldItem.mTitle == newItem.mTitle && oldItem.mPreview == newItem.mPreview && oldItem.mDateTime == newItem.mDateTime
            }
        }
    }

    private val mPlaceholder by lazy { Color.BLACK.toDrawable() }

    inner class LoadStateViewHolder(
        private val binding: ItemSavedStateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: LoadStateModelNovoSyx) {
            binding.tvName.text = model.mTitle
            binding.tvDateTime.text = model.mDateTime.formatDateTime("dd/MM/yyyy, hh:mm")
            mGlide.load(model.mPreview)
                .placeholder(mPlaceholder)
                .error(R.drawable.iv_load_error)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(binding.iv)

            itemView.setOnUnDoubleClick { onItemClick(model.iSlot) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadStateViewHolder {
        return LoadStateViewHolder(
            ItemSavedStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}