package com.dktech.baseandroidviewdktech.ui.myCollection

import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivityMyCollectionBinding

class MyCollectionActivity : BaseActivity<ActivityMyCollectionBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        }

    override fun getViewBinding(): ActivityMyCollectionBinding {
        return ActivityMyCollectionBinding.inflate(layoutInflater)
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