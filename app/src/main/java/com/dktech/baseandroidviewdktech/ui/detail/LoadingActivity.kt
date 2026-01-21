package com.dktech.baseandroidviewdktech.ui.detail

import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivityLoadingBinding

class LoadingActivity : BaseActivity<ActivityLoadingBinding>(){
    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        }

    override fun getViewBinding(): ActivityLoadingBinding {
        return ActivityLoadingBinding.inflate(layoutInflater)
    }

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }

}