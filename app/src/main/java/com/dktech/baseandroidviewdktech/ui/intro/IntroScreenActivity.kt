package com.dktech.baseandroidviewdktech.ui.intro

import android.app.Activity
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.LayoutActivityIntroBinding
import com.dktech.baseandroidviewdktech.ui.home.MainActivity

class IntroScreenActivity : BaseActivity<LayoutActivityIntroBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    override fun getViewBinding(): LayoutActivityIntroBinding {
        binding = LayoutActivityIntroBinding.inflate(layoutInflater)
        return binding
    }

    override fun initData() {
    }

    override fun shouldShowInternetDialog(): Boolean = false

    override fun initView() {
//        if (!AdmobUtils.isNetworkConnected(this)) {
//            Constants.isFailNativeFullScreen = true
//            Constants.isFailNativeFullScreen2 = true
//        }
        val mAdapter =
            IntroVP(
                this,
                onAClick = {
                    binding.container.currentItem = binding.container.currentItem + 1
                },
                onBClick = {
                    binding.container.currentItem = binding.container.currentItem + 1
                },
                onCClick = {
                    goToMain(this@IntroScreenActivity)
                },
                onNativeFragClick = {
                    try {
                        binding.container.currentItem = binding.container.currentItem + 1
                    } catch (e: Exception) {
                    }
                },
            ).apply {
//            updateListFragment()
            }

        binding.container.adapter = mAdapter
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }

    private fun goToMain(activity: Activity) {
        Intent(activity, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
//        runBlocking {
//            if(checkCameraPermission() && checkRecordAudio() ){
//
//            }else{
//                intentPermissionActivity.launch(
//                    Intent(activity, PermissionRequestActivity::class.java).apply {
//                        putExtra("fromSplash",true)
//                    }
//                )
//            }
//        }
    }
}
