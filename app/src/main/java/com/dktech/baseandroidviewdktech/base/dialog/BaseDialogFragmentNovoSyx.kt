package com.dktech.baseandroidviewdktech.base.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragmentNovoSyx<B : ViewBinding>(
    private val inflate: (LayoutInflater) -> B
) : DialogFragment() {
    protected val binding: B by lazy { inflate(layoutInflater) }

    /**
     * Avoid case dialog show after restore...
     */
    protected var isInit: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { mActivity ->
            val builder = AlertDialog.Builder(mActivity)
            builder.setView(binding.root)

            if (isInit) {
                initView()
                initActionView()
            }

            builder.create().apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

                this.setOnShowListener {
                    if (!isInit) {
                        dismiss()
                    }
                }
            }
        } ?: throw IllegalStateException("Activity can't null")
    }

    abstract fun initView()

    abstract fun initActionView()
}