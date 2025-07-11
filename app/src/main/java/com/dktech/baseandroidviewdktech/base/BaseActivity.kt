package com.dktech.baseandroidviewdktech.base

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseActivity<viewBinding: ViewBinding>
    (val inflater: (LayoutInflater) -> viewBinding): AppCompatActivity() {
    val binding: viewBinding by lazy {
        inflater(layoutInflater)
    }
    private val loadingDialog by lazy {
        LoadingDialog(this)
    }
    abstract val viewModel: BaseViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        setContentView(binding.root)
        initLoadingDialog()
        initData()
        initView()
        initEvent()
        initObserver()

    }
    abstract fun initData()
    abstract fun initView()
    abstract fun initEvent()
    abstract fun initObserver()

    private fun initLoadingDialog(){
        viewModel.loadingDialog.onEach {
            if (it) {
                loadingDialog.show()
            } else {
                loadingDialog.hide()
            }
        }.launchIn(lifecycleScope)
    }

}