package com.dktech.baseandroidviewdktech.ui.detail

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
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

    private val segmentParser by lazy {
        SegmentParser()
    }
    private lateinit var colorPickerAdapter: ColorPickerAdapter

    override val onBackPressedCallback: OnBackPressedCallback
        get() = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        }

    override fun getViewBinding(): ActivityDrawingBinding {
        return ActivityDrawingBinding.inflate(layoutInflater)
    }

    override fun initData() {
    }

    override fun initView() {
        val drawView = findViewById<DrawView>(R.id.drawview)
        setupColorPicker(drawView)

        lifecycleScope.launch {
            val svgFile =
                segmentParser.parseSVGFile(
                    this@DrawingActivity,
                    R.raw.line_paint_1,
                )
            drawView.initSegmentFile(svgFile)

            val uniqueColors = drawView.getUniqueColors()
            val colorToLayerMap = drawView.getColorToLayerMap()
            val colorItems =
                uniqueColors.map { color ->
                    ColorItem(color, colorToLayerMap[color] ?: 0)
                }
            colorPickerAdapter.submitList(colorItems)
            drawView.loadStrokeSvgFromResource(R.raw.line_paint_stroke_1)
            drawView.loadStrokePngFromResource(R.drawable.line_paint_1)
        }
    }
    private fun setupColorPicker(drawView: DrawView) {
        val recyclerView = findViewById<RecyclerView>(R.id.colorPicker)

        colorPickerAdapter =
            ColorPickerAdapter { selectedColor ->
                drawView.setSelectedColor(selectedColor)
            }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DrawingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorPickerAdapter
        }
    }
    override fun initEvent() {
    }

    override fun initObserver() {
    }
}