package com.dktech.baseandroidviewdktech.base.bottomsheet

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.databinding.DialogCollectionBinding
import com.dktech.baseandroidviewdktech.databinding.DialogConfirmActionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmDialog(
    val isDeleteOption: Boolean,
    val onConfirm: () -> Unit,
) : BottomSheetDialogFragment() {
    private var binding: DialogConfirmActionBinding? = null

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogConfirmActionBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (!isDeleteOption) {
            binding?.tvTitle?.text = getString(R.string.repaint_artwork)
            binding?.tvDescription?.text = getString(R.string.are_you_sure_to_repaint_this_artwork)
            binding?.tvAction?.text = getString(R.string.repaint)
            binding?.btnAction?.backgroundTintList = ColorStateList.valueOf("#0071C7".toColorInt())
        } else {
            binding?.tvTitle?.text = getString(R.string.delete_artwork)
            binding?.tvAction?.text = getString(R.string.delete)
            binding?.tvDescription?.text = getString(R.string.are_you_sure_to_delete_this_artwork)
            binding?.btnAction?.backgroundTintList = ColorStateList.valueOf("#E53A3A".toColorInt())
        }

        binding?.btnAction?.setOnClickListener {
            onConfirm()
            dismiss()
        }
        binding?.icClose?.setOnClickListener {
            dismiss()
        }
    }
}
