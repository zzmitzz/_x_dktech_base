package com.dktech.baseandroidviewdktech.ui.home.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.dktech.baseandroidviewdktech.base.BaseFragment
import com.dktech.baseandroidviewdktech.databinding.FragmentMainBinding
import com.dktech.baseandroidviewdktech.ui.detail.DrawingActivity
import com.dktech.baseandroidviewdktech.ui.detail.LoadingActivity
import com.dktech.baseandroidviewdktech.ui.home.MainViewModel
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import kotlinx.coroutines.launch

class ListHomeFragment : BaseFragment<FragmentMainBinding>() {
    private var paintID: String = ""
    private val viewModel by activityViewModels<MainViewModel>()

    private val loadingActivityLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Intent(this@ListHomeFragment.requireActivity(), DrawingActivity::class.java).apply {
                    putExtra(DrawingActivity.PAINTING_FILE_NAME, paintID)
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
        ItemAdapter { painting ->
            paintID = painting.fileName
            loadingActivityLauncher.launch(
                Intent(requireActivity(), LoadingActivity::class.java).apply {
                    putExtra(LoadingActivity.PAINTING_FILE_NAME, painting.fileName)
                    putExtra(LoadingActivity.FILL_SVG_URL, painting.fillSVG)
                    putExtra(LoadingActivity.STROKE_SVG_URL, painting.strokeSVG)
                },
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
        viewModel.loadColorBookData(requireContext())
    }

    override fun initEvent() {}

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataColorBook.collect { paintings ->
                    mAdapter.submitList(paintings)
                }
            }
        }
    }
}
