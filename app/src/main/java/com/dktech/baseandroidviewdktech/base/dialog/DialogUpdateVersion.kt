package com.dktech.baseandroidviewdktech.base.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.dktech.baseandroidviewdktech.base.BaseDialog
import com.dktech.baseandroidviewdktech.databinding.DialogUpdateVersionBinding
import kotlin.jvm.java

class DialogUpdateVersion(
    val mContext: Activity
) : BaseDialog<DialogUpdateVersionBinding>(
    {
        DialogUpdateVersionBinding.inflate(
            LayoutInflater.from(mContext)
        )
    },
    mContext,
    true
){
    override fun initView() {
    }

    override fun initData() {
    }

    override fun initActionView() {
        binding.btnUpdate.setOnClickListener {
//            AppOpenManager.getInstance().disableAppResumeWithActivity(mContext::class.java)
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${mContext.packageName}"))
                intent.setPackage("com.android.vending")
                mContext.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${mContext.packageName}"))
                    mContext.startActivity(intent)
                }catch (e: Exception){
                    Toast.makeText(mContext,"Error",Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.icClose.setOnClickListener {
            dismiss()
        }

    }

    override val layoutContainer: View
        get() = binding.root
}