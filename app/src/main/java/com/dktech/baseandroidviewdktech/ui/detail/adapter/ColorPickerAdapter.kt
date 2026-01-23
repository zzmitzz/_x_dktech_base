package com.dktech.baseandroidviewdktech.ui.detail.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.databinding.ItemColorBinding
import com.dktech.baseandroidviewdktech.model.ColorItem

class ColorPickerAdapter(
    private val onColorSelected: (ColorItem) -> Unit
) : ListAdapter<ColorItem, ColorPickerAdapter.ColorViewHolder>(ColorDiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ColorViewHolder(ItemColorBinding.inflate(layoutInflater))
    }


    fun updateSelectedPos(colorItem: ColorItem){
        var oldSelectPos = RecyclerView.NO_POSITION
        if(selectedPosition != -1){
            oldSelectPos = selectedPosition
        }
        selectedPosition = currentList.indexOf(colorItem)
        notifyItemChanged(selectedPosition)
        if(oldSelectPos != RecyclerView.NO_POSITION){
            notifyItemChanged(oldSelectPos)
        }
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val colorItem = getItem(position)
        holder.bind(colorItem, position == selectedPosition) { clickedPosition ->
            val previousPosition = selectedPosition
            selectedPosition = clickedPosition
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition)
            }
            notifyItemChanged(selectedPosition)
            onColorSelected(colorItem)
        }
    }

    class ColorViewHolder(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(colorItem: ColorItem, isSelected: Boolean, onClick: (Int) -> Unit) {
            binding.colorView.backgroundTintList = ColorStateList.valueOf(colorItem.color)
            binding.layerNumberText.text = colorItem.layerNumber.toString()
            binding.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                onClick(bindingAdapterPosition)
            }
        }
    }

    class ColorDiffCallback : DiffUtil.ItemCallback<ColorItem>() {
        override fun areItemsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
            return oldItem.color == newItem.color
        }

        override fun areContentsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
            return oldItem == newItem
        }
    }
}
