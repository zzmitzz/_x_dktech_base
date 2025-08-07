package com.dktech.baseandroidviewdktech.ui.splash_screen

import android.content.Intent
import android.os.Handler
import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.BaseViewModel
import com.dktech.baseandroidviewdktech.databinding.LayoutActivitySplashscreenBinding
import com.dktech.baseandroidviewdktech.ui.language_screen.LanguageScreenActivity

class SplashScreenActivity : BaseActivity<LayoutActivitySplashscreenBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }

    override fun getViewBinding(): LayoutActivitySplashscreenBinding {
        return LayoutActivitySplashscreenBinding.inflate(layoutInflater)
    }

    override fun initData() {

    }

    override fun initView() {
        Handler().postDelayed(Runnable {
            startActivity(Intent(this, LanguageScreenActivity::class.java))
            finish()
        }, 2000)
    }

    override fun initEvent() {
    }

    override fun initObserver() {

    }

}