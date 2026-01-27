package com.dktech.baseandroidviewdktech.ui.splash_screen

import android.content.Intent
import android.os.Handler
import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.BaseViewModel
import com.dktech.baseandroidviewdktech.data.remote.RetrofitClient
import com.dktech.baseandroidviewdktech.databinding.LayoutActivitySplashscreenBinding
import com.dktech.baseandroidviewdktech.ui.home.MainActivity
import com.dktech.baseandroidviewdktech.ui.language_screen.LanguageScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SplashScreenActivity : BaseActivity<LayoutActivitySplashscreenBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                }
            }

    override fun getViewBinding(): LayoutActivitySplashscreenBinding = LayoutActivitySplashscreenBinding.inflate(layoutInflater)

    override fun initData() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                withTimeout(10000) {
                    RetrofitClient.getColoringBookData()
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun initView() {
        nextActivity()
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }

    fun nextActivity() {
        Handler().postDelayed(
            Runnable {
                startActivity(Intent(this, LanguageScreenActivity::class.java))
                finish()
            },
            2000,
        )
    }
}
