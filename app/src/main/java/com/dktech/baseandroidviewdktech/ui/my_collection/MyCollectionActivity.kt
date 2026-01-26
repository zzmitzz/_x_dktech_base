package com.dktech.baseandroidviewdktech.ui.my_collection

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivityMyCollectionBinding
import com.dktech.baseandroidviewdktech.ui.detail.LoadingActivity
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import com.dktech.baseandroidviewdktech.utils.Constants
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener

class MyCollectionActivity : BaseActivity<ActivityMyCollectionBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    private val mAdapter by lazy {
        ItemAdapter(
            emptyList(),
        ) { painting ->
        }
    }

    override fun getViewBinding(): ActivityMyCollectionBinding = ActivityMyCollectionBinding.inflate(layoutInflater)

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initEvent() {
        binding.imageView.setSafeOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }

    override fun initObserver() {
    }
}
