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
import java.io.File

class ListHomeFragment : BaseFragment<FragmentMainBinding>() {
    private var paintID: String = ""
    private val viewModel by activityViewModels<MainViewModel>()

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
            Intent(requireActivity(), LoadingActivity::class.java).apply {
                putExtra(
                    LoadingActivity.CACHE_FILE,
                    painting.cacheThumb,
                )
                putExtra(LoadingActivity.REMOTE_URL, painting.remoteThumb)
                putExtra(LoadingActivity.PAINTING_FILE_NAME, painting.fileName)
                putExtra(LoadingActivity.FILL_SVG_URL, painting.fillSVG)
                putExtra(LoadingActivity.STROKE_SVG_URL, painting.strokeSVG)
                startActivity(this)
            }
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
