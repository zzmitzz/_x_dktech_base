package com.dktech.baseandroidviewdktech.ui.my_collection

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.bottomsheet.ConfirmDialog
import com.dktech.baseandroidviewdktech.base.dialog.CollectionDialog
import com.dktech.baseandroidviewdktech.databinding.ActivityMyCollectionBinding
import com.dktech.baseandroidviewdktech.ui.detail.DrawingActivity
import com.dktech.baseandroidviewdktech.ui.detail.LoadingActivity
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.Constants
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import kotlinx.coroutines.launch
import java.io.File

class MyCollectionActivity : BaseActivity<ActivityMyCollectionBinding>() {
    private lateinit var paintID: PaintingUIWrapper
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    private val viewModel by viewModels<MyCollectionVM>()
    private val listener =
        object : CollectionDialog.OnCallbackAction {
            override fun onNextAction() {
                Intent(this@MyCollectionActivity, LoadingActivity::class.java).apply {
                    putExtra(LoadingActivity.CACHE_FILE, paintID.cacheThumb)
                    putExtra(LoadingActivity.PAINTING_FILE_NAME, paintID.fileName)
                    putExtra(LoadingActivity.FILL_SVG_URL, paintID.fillSVG)
                    putExtra(LoadingActivity.STROKE_SVG_URL, paintID.strokeSVG)
                    startActivity(this)
                }
            }

            override fun onResetAction() {
                ConfirmDialog(
                    false,
                ) {
                    lifecycleScope.launch {
                        viewModel.deleteColoredSegments(paintID.fileName)
                        deleteCachedBitmap(paintID.fileName)
                        Intent(this@MyCollectionActivity, LoadingActivity::class.java).apply {
                            putExtra(LoadingActivity.PAINTING_FILE_NAME, paintID.fileName)
                            putExtra(LoadingActivity.FILL_SVG_URL, paintID.fillSVG)
                            putExtra(LoadingActivity.STROKE_SVG_URL, paintID.strokeSVG)
                            startActivity(this)
                        }
                    }
                }.show(supportFragmentManager, "ConfirmDialogReset")
            }

            override fun onDeleteAction() {
                ConfirmDialog(
                    true,
                ) {
                    lifecycleScope.launch {
                        viewModel.deleteColoredSegments(paintID.fileName)
                        deleteCachedBitmap(paintID.fileName)
                    }
                }.show(supportFragmentManager, "ConfirmDialogDelete")
            }
        }
    private val mAdapter by lazy {
        ItemAdapter { painting ->
            paintID = painting
            CollectionDialog(
                false,
                cacheImage = painting.cacheThumb ?: "".toUri(),
                listener,
            ).show(supportFragmentManager, "CollectionDialog")
        }
    }

    override fun getViewBinding(): ActivityMyCollectionBinding = ActivityMyCollectionBinding.inflate(layoutInflater)

    override fun initData() {
        viewModel.loadData(this)
    }

    override fun initView() {
        binding.rcvPainting.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(this@MyCollectionActivity, 2)
        }
    }

    override fun initEvent() {
        binding.imageView.setSafeOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.listData.collect {
                    mAdapter.submitList(it)
                }
            }
        }
    }

    private fun deleteCachedBitmap(fileName: String) {
        val cacheFileName = "$fileName.png"
        val cacheFile = File(cacheDir, cacheFileName)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }
}
