package com.dktech.baseandroidviewdktech.ui.my_collection

import android.content.Intent
import android.view.View
import androidx.activity.OnBackPressedCallback
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
import com.dktech.baseandroidviewdktech.ui.detail.LoadingActivity
import com.dktech.baseandroidviewdktech.ui.home.adapter.ItemAdapter
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import com.dktech.baseandroidviewdktech.utils.helper.toJsonWithTypeToken
import com.google.gson.Gson
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
                    val serializedPaint = Gson().toJsonWithTypeToken(paintID)
                    putExtra(LoadingActivity.PAINTING, serializedPaint)
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
                            val serializedPaint = Gson().toJsonWithTypeToken(paintID)
                            putExtra(LoadingActivity.PAINTING, serializedPaint)
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
                cacheImage = painting.cacheThumb?.toUri() ?: "".toUri(),
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
                    if (it.isNotEmpty()) {
                        binding.llEmpty.visibility = View.GONE
                        mAdapter.submitList(it)
                    } else {
                        binding.llEmpty.visibility = View.VISIBLE
                    }
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
