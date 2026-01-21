package com.dktech.baseandroidviewdktech.ui.detail.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.model.ColorItem

class ColorPickerAdapter(
    private val onColorSelected: (Int) -> Unit
) : ListAdapter<ColorItem, ColorPickerAdapter.ColorViewHolder>(ColorDiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
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
            
            onColorSelected(colorItem.color)
        }
    }

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.colorView)
        private val selectionIndicator: View = itemView.findViewById(R.id.selectionIndicator)
        private val layerNumberText: TextView = itemView.findViewById(R.id.layerNumberText)

        fun bind(colorItem: ColorItem, isSelected: Boolean, onClick: (Int) -> Unit) {
            colorView.setBackgroundColor(colorItem.color)
            
            if (colorItem.color == Color.WHITE) {
                colorView.setBackgroundColor(Color.LTGRAY)
            }
            
            layerNumberText.text = colorItem.layerNumber.toString()
            
            selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            itemView.setOnClickListener {
                onClick(adapterPosition)
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
