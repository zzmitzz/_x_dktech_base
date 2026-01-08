package com.dktech.baseandroidviewdktech.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.viewbinding.ViewBinding
import com.emulator.retro.console.game.R
import com.emulator.retro.console.game.utils.setFullScreenVisibility

abstract class BaseDialogNovoSyx<B : ViewBinding>(
    bindingFactory: (LayoutInflater) -> B,
    mContext: Context,
    private val cancelable: Boolean = false
) : Dialog(mContext, R.style.Theme_BaseProject) {

    protected val binding: B by lazy { bindingFactory(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setFullScreenVisibility()
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(binding.root)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        setCancelable(cancelable)

        if (!cancelable) {
            setCanceledOnTouchOutside(false)
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }

        initView()
        initData()
        initActionView()

        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
        binding.root.setOnClickListener {
            if (cancelable) dismiss()
        }


    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window?.setFullScreenVisibility()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (cancelable) dismiss()
    }

    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initActionView()
}