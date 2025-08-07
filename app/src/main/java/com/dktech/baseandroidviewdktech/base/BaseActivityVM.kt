package com.dktech.baseandroidviewdktech.base

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.dktech.baseandroidviewdktech.base.dialog.LoadingDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.reflect.ParameterizedType

abstract class BaseActivityVM<VM : BaseViewModel, VB : ViewBinding> : BaseActivity<VB>() {
    protected lateinit var viewModel: VM

    private val loadingDialog by lazy {
        LoadingDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        initLoadingDialog()
    }

    abstract fun initViewModel()
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