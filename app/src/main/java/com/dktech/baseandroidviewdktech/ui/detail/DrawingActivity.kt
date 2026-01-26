package com.dktech.baseandroidviewdktech.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
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
import com.dktech.baseandroidviewdktech.base.dialog.SettingDrawingBTS
import com.dktech.baseandroidviewdktech.core.custom_view.DrawView
import com.dktech.baseandroidviewdktech.databinding.ActivityDrawingBinding
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.ui.detail.adapter.ColorPickerAdapter
import com.dktech.baseandroidviewdktech.ui.finish.ResultActivity
import com.dktech.baseandroidviewdktech.utils.Constants
import com.dktech.baseandroidviewdktech.utils.helper.FileHelper
import com.dktech.baseandroidviewdktech.utils.helper.getBooleanPrefs
import com.dktech.baseandroidviewdktech.utils.helper.setBooleanPrefs
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class DrawingActivity : BaseActivity<ActivityDrawingBinding>() {
    private val viewModel by viewModels<DrawingVM>()
    private lateinit var colorPickerAdapter: ColorPickerAdapter

    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    private val fileHelper by lazy {
        FileHelper(this@DrawingActivity)
    }

    override fun getViewBinding(): ActivityDrawingBinding =
        ActivityDrawingBinding.inflate(layoutInflater)

    override fun initData() {
        val paintID = intent.getIntExtra(PAINTING_ID, -1)
        if(paintID != -1){
            val paint = Constants.mockListData.find { it.id  == paintID}
            if(paint != null){
                preparingData(
                    strokeSVG = paint.strokeFileName,
                    fillSVG = paint.fillFileName
                )
            }else{
                Toast.makeText(this, "Couldn't find the painting, please try again later.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val PAINTING_ID = "INTENT_PAINTING_ID"
    }

    private val handler = Handler(Looper.getMainLooper())
    val saveBitmapThumbnailRunnable =
        Runnable {
            viewModel.onScreenLeaving(cacheDir) {
                runOnUiThread {
                    if (viewModel.configUIState.value.showPreview) {
                        updatePreviewBitmap()
                    }
                }
            }
        }

    private fun updatePreviewBitmap() {
        binding.imView.initBitmap("line_paint_2.png")
    }

    private val onDrawColorToSegment: ((Int) -> Unit) = { segmentID ->
        viewModel.colorSegment(segmentID)
        handler.removeCallbacks(saveBitmapThumbnailRunnable)
        handler.postDelayed(saveBitmapThumbnailRunnable, 500L)
        // update the color picker list.
        val segment =
            viewModel.drawingUIState.value.segmentUIState
                .find { it.id == segmentID }
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
        binding.drawview.onViewportChangeCallback = { state ->
            viewModel.updateScaleToShowPreview(
                state.scale > state.originalScale
            )
            binding.imView.updateViewport(state)
        }
        viewModel.initConfiguration()
        viewModel.saveSetting(
            getBooleanPrefs(Constants.configPreview, false),
            getBooleanPrefs(Constants.configVibration, false)
        )
        handler.postDelayed(saveBitmapThumbnailRunnable, 500L)

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
            viewModel.onScreenLeaving(cacheDir) {}
            onBackPressedCallback.handleOnBackPressed()
        }
        binding.icSetting.setSafeOnClickListener {
            SettingDrawingBTS(
                initState =
                    getBooleanPrefs(
                        Constants.configPreview,
                        false,
                    ) to getBooleanPrefs(Constants.configVibration, false),
                onSave = {
                    setBooleanPrefs(Constants.configPreview, it.first)
                    setBooleanPrefs(Constants.configVibration, it.second)
                    viewModel.saveSetting(it.first, it.second)
                },
            ).show(
                supportFragmentManager,
                "SettingDrawingBTS",
            )
        }
    }

    private fun updateProgressBar() {
        val listData = viewModel.drawingUIState.value.segmentUIState
        if (listData.isEmpty()) return
        val finishPercent =
            (listData.filter { it.isColored }.size / listData.size.toFloat()) * 100
        binding.progressBar.setProgress(finishPercent.toInt(), true)
        binding.tvProgress.text = String.format("%.2f%% ", finishPercent)
    }

    override fun onStop() {
        super.onStop()
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
                    binding.previewView.visibility =
                        if (it.showPreview && it.shouldShowPreviewDueScaling) View.VISIBLE else View.GONE

                }
            }
        }
    }

    private fun preparingData(
        fileName: String = "line_paint_2",
        strokeSVG: String = "line_paint_2_stroke.svg",
        fillSVG: String = "line_paint_2.svg",
    ) {
        val strokePicture = fileHelper.parseAssetFileToPicture(strokeSVG)
        viewModel.initSegmentDraw(this@DrawingActivity, fillSVG, strokePicture)
        binding.drawview.loadStrokeSvgFromResource(strokePicture)
    }
}
