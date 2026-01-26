package com.dktech.baseandroidviewdktech.ui.detail

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivityLoadingBinding
import kotlinx.coroutines.android.HandlerDispatcher

class LoadingActivity : BaseActivity<ActivityLoadingBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    override fun getViewBinding(): ActivityLoadingBinding = ActivityLoadingBinding.inflate(layoutInflater)

    override fun initData() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                setResult(Activity.RESULT_OK, Intent())
                finish()
            },
            3000,
        )
    }

    override fun initView() {
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }
}
