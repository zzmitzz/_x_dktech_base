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

abstract class BaseActivity<viewBinding: ViewBinding>: AppCompatActivity() {
    protected lateinit var binding: viewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        binding = getViewBinding()
        setContentView(binding.root)
        initData()
        initView()
        initEvent()
        initObserver()
    }
    abstract fun getViewBinding(): viewBinding
    abstract fun initData()
    abstract fun initView()
    abstract fun initEvent()
    abstract fun initObserver()


    companion object {
        val TAG = this::class.java.simpleName
    }

}