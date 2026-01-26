package com.dktech.baseandroidviewdktech.ui.home.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.dktech.baseandroidviewdktech.base.BaseFragment
import com.dktech.baseandroidviewdktech.databinding.FragmentMainBinding
import com.dktech.baseandroidviewdktech.ui.detail.DrawingActivity
import com.dktech.baseandroidviewdktech.ui.detail.LoadingActivity
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import com.dktech.baseandroidviewdktech.utils.Constants

class ListHomeFragment : BaseFragment<FragmentMainBinding>() {
    var paintID = -1

    private val loadingActivityLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Intent(this@ListHomeFragment.requireActivity(), DrawingActivity::class.java).apply {
                    putExtra(DrawingActivity.PAINTING_ID, paintID)
                    startActivity(this)
                }
            }
        }

    companion object {
        fun newInstance(dataOrder: Int): ListHomeFragment =
            ListHomeFragment().apply {
                arguments =
                    Bundle().apply {
                        putInt("dataOrder", dataOrder)
                    }
            }
    }

    private val mAdapter by lazy {
        ItemAdapter(
            Constants.mockListData,
        ) { painting ->
            paintID = painting.id
            loadingActivityLauncher.launch(
                Intent(requireActivity(), LoadingActivity::class.java),
            )
        }
    }

    override fun getViewBinding(): FragmentMainBinding = FragmentMainBinding.inflate(layoutInflater)

    override fun initView() {
        binding.rcvPainting.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2)
        }
    }

    override fun initData() {
    }

    override fun initEvent() {
    }
}
