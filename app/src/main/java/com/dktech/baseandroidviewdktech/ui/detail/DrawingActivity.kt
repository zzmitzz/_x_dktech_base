package com.dktech.baseandroidviewdktech.ui.detail

import android.annotation.SuppressLint
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.core.custom_view.DrawView
import com.dktech.baseandroidviewdktech.databinding.ActivityDrawingBinding
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.ui.detail.adapter.ColorPickerAdapter
import kotlinx.coroutines.launch

class DrawingActivity : BaseActivity<ActivityDrawingBinding>() {

    private val viewModel by viewModels<DrawingVM>()
    private lateinit var colorPickerAdapter: ColorPickerAdapter

    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    override fun getViewBinding(): ActivityDrawingBinding {
        return ActivityDrawingBinding.inflate(layoutInflater)
    }

    override fun initData() {

    }

    @SuppressLint("DefaultLocale")
    override fun initView() {
        setupColorPicker(binding.drawview)

        binding.drawview.setCallbackOnColor { color ->
            // :)
            viewModel.incColored()
            val finishPercent =
                (viewModel.coloredSegment.toFloat() / viewModel.totalSegments.toFloat()) * 100

            binding.progressBar.setProgress(finishPercent.toInt(),true)
            binding.tvProgress.text = String.format("%.2f%%", finishPercent)


            val colorItem = colorPickerAdapter.currentList.find { it.color == color }
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
        viewModel.initSegmentDraw(this@DrawingActivity, R.raw.line_3)
        viewModel.initConfiguration()
        binding.drawview.loadStrokeSvgFromResource(R.raw.line_3_stroke)
        binding.drawview.loadStrokePngFromResource(R.drawable.line_3_stroke)
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

    }

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.drawingUIState.collect {
                    colorPickerAdapter.submitList(it.color)
                    binding.drawview.initSegmentFile(it.svgWidth, it.svgHeight, it.segmentUIState)
                    if (it.color.isNotEmpty() && viewModel.configUIState.value.currentSelectedColor == null) {
                        viewModel.setColor(it.color.first())
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
}