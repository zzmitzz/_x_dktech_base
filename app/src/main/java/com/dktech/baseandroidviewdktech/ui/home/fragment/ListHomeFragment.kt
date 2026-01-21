package com.dktech.baseandroidviewdktech.ui.home.fragment

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.dktech.baseandroidviewdktech.base.BaseFragment
import com.dktech.baseandroidviewdktech.databinding.FragmentMainBinding
import com.dktech.baseandroidviewdktech.ui.detail.DrawingActivity
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import com.dktech.baseandroidviewdktech.utils.Constants

class ListHomeFragment : BaseFragment<FragmentMainBinding>() {


    companion object {
        fun newInstance(dataOrder: Int): ListHomeFragment{
            return ListHomeFragment().apply {
                arguments = Bundle().apply {
                    putInt("dataOrder", dataOrder)
                }
            }
        }
    }

    private val mAdapter by lazy {
        ItemAdapter(
            Constants.mockListData
        ){
            Intent(this@ListHomeFragment.requireActivity(), DrawingActivity::class.java).apply{
                startActivity(this)
            }
        }
    }

    override fun getViewBinding(): FragmentMainBinding {
        return FragmentMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rcvPainting.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(binding.root.context,2)
        }
    }

    override fun initData() {
    }

    override fun initEvent() {
    }

}