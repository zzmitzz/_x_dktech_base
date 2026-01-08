package com.emulator.retro.console.game.retro_game.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.emulator.retro.console.game.databinding.ItemDialogSettingOptionBinding
import com.emulator.retro.console.game.utils.setOnUnDoubleClick

class OptionDialogAdapterNovoSyx(
    private val mData: List<String>,
    mSelected: Int,
    private val onChanged: (Int) -> Unit
): RecyclerView.Adapter<OptionDialogAdapterNovoSyx.OptionDialogViewHolder>() {
    private var mISelected = mSelected
        set(value) {
            if (field == value) return

            field = value
            onChanged(field)
        }

    inner class OptionDialogViewHolder(private val binding: ItemDialogSettingOptionBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {
            binding.tv.text = value

            val isSelected = absoluteAdapterPosition == mISelected
            binding.tv.isSelected = isSelected
            binding.tv.setTextColor((if (isSelected) "#89FB24" else "#80FFFFFF").toColorInt())

            itemView.setOnUnDoubleClick(true) {
                if (isSelected) return@setOnUnDoubleClick

                val old = mISelected
                mISelected = absoluteAdapterPosition
                if (old in mData.indices) notifyItemChanged(old)
                notifyItemChanged(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionDialogViewHolder {
        return OptionDialogViewHolder(
            ItemDialogSettingOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: OptionDialogViewHolder, position: Int) {
        holder.bind(mData[position])
    }

}