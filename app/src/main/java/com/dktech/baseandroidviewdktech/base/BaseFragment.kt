package com.dktech.baseandroidviewdktech.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseFragment<viewBinding : ViewBinding>(
    private val inflate: (inflater: LayoutInflater) -> viewBinding
) : Fragment() {
    private val binding by lazy {
        inflate(layoutInflater)
    }
    private val loadingDialog by lazy {
        LoadingDialog(requireContext())
    }

    abstract var viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    open fun observeData() {
        viewModel.loadingDialog.onEach {
            if (it) {
                loadingDialog.show()
            } else {
                loadingDialog.hide()
            }
        }.launchIn(lifecycleScope)
    }

}