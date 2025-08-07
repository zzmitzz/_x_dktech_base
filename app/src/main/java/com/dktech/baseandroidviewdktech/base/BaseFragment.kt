package com.dktech.baseandroidviewdktech.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.dktech.baseandroidviewdktech.base.dialog.LoadingDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseFragment<viewBinding : ViewBinding>() : Fragment() {
    private var _binding: viewBinding? = null
    protected val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding()
        initView()
        initEvent()
        observeData()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    abstract fun getViewBinding(): viewBinding
    abstract fun initView()
    abstract fun initData()
    abstract fun initEvent()

    open fun observeData(){

    }

    companion object {
        val TAG = this.javaClass.simpleName.toString()
    }

}