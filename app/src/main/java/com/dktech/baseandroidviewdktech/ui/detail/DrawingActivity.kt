package com.dktech.baseandroidviewdktech.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.core.custom_view.DrawView
import com.dktech.baseandroidviewdktech.databinding.ActivityDrawingBinding
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.ui.detail.adapter.ColorPickerAdapter
import com.dktech.baseandroidviewdktech.ui.finish.ResultActivity
import com.dktech.baseandroidviewdktech.utils.helper.FileHelper
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class DrawingActivity : BaseActivity<ActivityDrawingBinding>() {

    private val viewModel by viewModels<DrawingVM>()
    private lateinit var colorPickerAdapter: ColorPickerAdapter

    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    private val fileHelper by lazy {
        FileHelper(this@DrawingActivity)
    }

    override fun getViewBinding(): ActivityDrawingBinding {
        return ActivityDrawingBinding.inflate(layoutInflater)
    }

    override fun initData() {

    }


    private val onDrawColorToSegment: ((Int) -> Unit) = { segmentID ->

        viewModel.colorSegment(segmentID)
        val segment = viewModel.drawingUIState.value.segmentUIState.find { it.id == segmentID }
        val colorItem = colorPickerAdapter.currentList.find { it.color == segment?.targetColor }
        colorItem?.let {
            if (it.freqShown == 1) {
                val newList = colorPickerAdapter.currentList.toMutableList()
                newList.remove(colorItem)
                colorPickerAdapter.submitList(newList)
                if (newList.isNotEmpty()) {
                    viewModel.setColor(newList.first())
                }
            } else {
                val newList = colorPickerAdapter.currentList.toMutableList()
                val index = colorPickerAdapter.currentList.indexOf(colorItem)
                newList[index] = colorItem.copy(freqShown = colorItem.freqShown - 1)
                colorPickerAdapter.submitList(newList)
            }
        }
    }


    @SuppressLint("DefaultLocale")
    override fun initView() {
        setupColorPicker(binding.drawview)

        binding.drawview.setCallbackOnColor(onDrawColorToSegment)
        viewModel.initConfiguration()
        preparingData()
    }

    private fun setupColorPicker(drawView: DrawView) {
        colorPickerAdapter =
            ColorPickerAdapter { selectedColor ->
                viewModel.setColor(selectedColor)
            }
        binding.colorPicker.apply {
            layoutManager =
                LinearLayoutManager(this@DrawingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorPickerAdapter
        }
    }

    override fun initEvent() {
        binding.icBack.setSafeOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }


    private fun updateProgressBar() {
        val listData = viewModel.drawingUIState.value.segmentUIState
        if (listData.isEmpty()) return
        val finishPercent =
            (listData.filter { it.isColored }.size / listData.size.toFloat()) * 100
        binding.progressBar.setProgress(finishPercent.toInt(), true)
        binding.tvProgress.text = String.format("%.2f%%", finishPercent)
    }


    private fun finishDrawEffect() {
        Intent(this@DrawingActivity, ResultActivity::class.java).apply {
            startActivity(this)
        }
        finish()

    }

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.drawingUIState.collect {
                    colorPickerAdapter.submitList(it.color)
                    binding.drawview.initSegmentFile(it.svgWidth, it.svgHeight, it.segmentUIState)
//                    if (it.color.isNotEmpty() && viewModel.configUIState.value.currentSelectedColor == null) {
//                        viewModel.setColor(it.color.first())
//                    }
                    updateProgressBar()
                    val data = viewModel.drawingUIState.value.segmentUIState
                    if (data.isNotEmpty() && data.all { seg -> seg.isColored }) {
                        finishDrawEffect()
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.configUIState.collect {
                    if (it.currentSelectedColor != null) {
                        binding.drawview.setSelectedColor(it.currentSelectedColor.color)
                        colorPickerAdapter.updateSelectedPos(it.currentSelectedColor)
                    }
                }
            }
        }

    }


    private fun preparingData(
        strokeSVG: String = "line_3_stroke.svg",
        strokePNG: String = "line_3_stroke.png",
        fillSVG: String = "line_3.svg"
    ) {
        viewModel.initSegmentDraw(this@DrawingActivity, fillSVG)
        binding.drawview.loadStrokeSvgFromResource(fileHelper.parseAssetFileToPicture(strokeSVG))
        fileHelper.parseAssetPNGFile(strokePNG)?.let {
            binding.drawview.loadStrokePngFromResource(it)
        }
    }
}