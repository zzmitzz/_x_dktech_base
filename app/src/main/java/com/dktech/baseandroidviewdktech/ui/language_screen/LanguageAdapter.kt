package com.dktech.baseandroidviewdktech.ui.language_screen

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.ui_models.LanguageModel
import com.dktech.baseandroidviewdktech.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val context: Context,
    private val languageList: List<LanguageModel>,
    private val onFirstSelect: () -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var selectedLanguage: LanguageModel? = null
        set(value) {
            if (field == null) {
                onFirstSelect()
            }
            field = value
        }

    inner class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: LanguageModel) {

            val isSelected = selectedLanguage == language
            binding.root.background =
                AppCompatResources.getDrawable(
                    context,
                    if (isSelected) R.drawable.bg_language_selected else R.drawable.bg_language_unselected
                )
            binding.ivLanguage.setImageResource(language.flagImage)
            binding.languageName.text = context.getString(language.name)
            binding.languageName.setHorizontallyScrolling(true)
            binding.ivRadio.setImageResource(
                if (isSelected) {
                    R.drawable.ic_rbtn_2_checked
                } else {
                    R.drawable.ic_rbtn_2
                }
            )
            binding.root.setOnClickListener {
                selectedLanguage = language
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemLanguageBinding.inflate(inflater, parent, false)
        )
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languageList[position]
        holder.bind(language)
    }

    fun getSelectedPositionLanguage(): LanguageModel? {
        return selectedLanguage
    }

    fun setSelectedPositionLanguage(language: LanguageModel) {
        selectedLanguage = language
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

}