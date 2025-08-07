package com.dktech.baseandroidviewdktech.ui.home

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dktech.baseandroidviewdktech.MainViewModel
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivityVM
import com.dktech.baseandroidviewdktech.base.ViewModelFactory
import com.dktech.baseandroidviewdktech.databinding.ActivityMainBinding

class MainActivity : BaseActivityVM<MainViewModel, ActivityMainBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelFactory())[MainViewModel::class.java]
    }

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
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