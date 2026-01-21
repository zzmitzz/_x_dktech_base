package com.dktech.baseandroidviewdktech.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dktech.baseandroidviewdktech.databinding.ItemPaintMainBinding
import com.dktech.baseandroidviewdktech.utils.Painting
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class ItemAdapter(
    val listItem: List<Painting>,
    val onClick: (Painting) -> Unit,
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(ItemPaintMainBinding.inflate(layoutInflater))
    }
    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    inner class ItemViewHolder(val binding: ItemPaintMainBinding) : RecyclerView.ViewHolder(binding.root){
        fun onBind(){
            val item = listItem[bindingAdapterPosition]
            binding.root.setSafeOnClickListener {
                onClick(item)
            }
            binding.imageLine.setImageResource(item.overLayLinePaint)
        }
    }
}