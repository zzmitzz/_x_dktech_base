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
import com.dktech.baseandroidviewdktech.base.dialog.LoadingDialog
import com.dktech.baseandroidviewdktech.databinding.FragmentMainBinding
import com.dktech.baseandroidviewdktech.ui.detail.DrawingActivity
import com.dktech.baseandroidviewdktech.ui.detail.LoadingActivity
import com.dktech.baseandroidviewdktech.ui.home.MainViewModel
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingCategory
import com.dktech.baseandroidviewdktech.utils.helper.gone
import com.dktech.baseandroidviewdktech.utils.helper.toJsonWithTypeToken
import com.dktech.baseandroidviewdktech.utils.helper.visible
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File

class ListHomeFragment : BaseFragment<FragmentMainBinding>() {
    private val viewModel by activityViewModels<MainViewModel>()

    private val mAdapter by lazy {
        ItemAdapter { painting ->
            Intent(requireActivity(), LoadingActivity::class.java).apply {
                val serializedPaint = Gson().toJsonWithTypeToken(painting)
                putExtra(LoadingActivity.PAINTING, serializedPaint)
                startActivity(this)
            }
        }
    }

    private val category: PaintingCategory by lazy {
        PaintingCategory.entries[requireArguments().getInt(ARG_ID)]
    }

    companion object {
        private const val ARG_ID = "category_index"

        fun newInstance(id: Int): ListHomeFragment =
            ListHomeFragment().apply {
                arguments =
                    Bundle().apply {
                        putInt(ARG_ID, id)
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
    }

    override fun initEvent() {}

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataColorBook.collect { paintings ->
                    mAdapter.submitList(
                        paintings.filter {
                            it.category == category
                        },
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collect {
                    if (it) {
                        binding.loadingProgress.visible()
                        binding.rcvPainting.gone()
                    } else {
                        binding.loadingProgress.gone()
                        binding.rcvPainting.visible()
                    }
                }
            }
        }
    }
}
