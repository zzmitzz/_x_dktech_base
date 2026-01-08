package com.emulator.retro.console.game.retro_game.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emulator.retro.console.game.databinding.ItemSettingCoreBinding
import com.emulator.retro.console.game.retro_game.pauseMenuAction.EmulairCoreOptionNovoSyx
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class CoreOptionAdapterNovoSyx(
    private val getValueSelected: (EmulairCoreOptionNovoSyx) -> String,
    private val onItemClick: (EmulairCoreOptionNovoSyx) -> Unit,
) : ListAdapter<EmulairCoreOptionNovoSyx, CoreOptionAdapterNovoSyx.CoreOptionViewHolder>(EmulairCoreOptionNovoSyx.DiffCallback) {

    inner class CoreOptionViewHolder(private val binding: ItemSettingCoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(option: EmulairCoreOptionNovoSyx) {
            binding.tvTitle.setText(option.getDisplayName())
            binding.tvValue.text = upperFirstLetter(getValueSelected(option))

            itemView.setOnUnDoubleClick(true) { onItemClick(option) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoreOptionViewHolder {
        return CoreOptionViewHolder(
            ItemSettingCoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CoreOptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun upperFirstLetter(str: String): String {
        if (str.isEmpty() || !str.first().isLetter() || str.first().isUpperCase()) return str

        val firstLetter = str.first()
        return str.replaceFirst(firstLetter, firstLetter.uppercaseChar())
    }
}