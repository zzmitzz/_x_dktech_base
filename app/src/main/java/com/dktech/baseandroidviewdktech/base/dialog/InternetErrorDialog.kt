package com.dktech.baseandroidviewdktech.base.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.dktech.baseandroidviewdktech.base.BaseDialog
import com.dktech.baseandroidviewdktech.databinding.DialogInternetConnectBinding

class InternetErrorDialog(
    private val context: Context
) : BaseDialog<DialogInternetConnectBinding>(
    {
        DialogInternetConnectBinding.inflate(
            LayoutInflater.from(context)
        )
    },
    context,
    cancelable = false
) {


    var onRetry : () -> Unit = {}
    var onSettings : () -> Unit = {}

    override fun initView() {
    }

    override fun initData() {
    }
    override fun initActionView() {
        binding.btnSet.setOnClickListener {
            onRetry()
        }
    }

    fun show(onRetry: () -> Unit, onSettings: () -> Unit) {
        this.onRetry = onRetry
        this.onSettings = onSettings
        super.show()
    }
    override val layoutContainer: View
        get() = binding.root
}