package com.dktech.baseandroidviewdktech.ui.my_collection

import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivityMyCollectionBinding

class MyCollectionActivity : BaseActivity<ActivityMyCollectionBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    override fun getViewBinding(): ActivityMyCollectionBinding = ActivityMyCollectionBinding.inflate(layoutInflater)

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }
}
