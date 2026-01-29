package com.dktech.baseandroidviewdktech.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Picture
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
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
import com.dktech.baseandroidviewdktech.base.bottomsheet.SelectMusicBottomSheet
import com.dktech.baseandroidviewdktech.base.bottomsheet.model.MusicItem
import com.dktech.baseandroidviewdktech.base.dialog.SettingDrawingBTS
import com.dktech.baseandroidviewdktech.core.custom_view.DrawView
import com.dktech.baseandroidviewdktech.core.custom_view.ViewportState
import com.dktech.baseandroidviewdktech.databinding.ActivityDrawingBinding
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.model.Painting
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.ui.detail.adapter.ColorPickerAdapter
import com.dktech.baseandroidviewdktech.ui.finish.ResultActivity
import com.dktech.baseandroidviewdktech.utils.Constants
import com.dktech.baseandroidviewdktech.utils.helper.FileHelper
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoFillSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoStrokeSVG
import com.dktech.baseandroidviewdktech.utils.helper.getBooleanPrefs
import com.dktech.baseandroidviewdktech.utils.helper.setBooleanPrefs
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.abs

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

    private var musicPlayer: MediaPlayer? = null
    private var selectedMusic: MusicItem? = null

    override fun getViewBinding(): ActivityDrawingBinding =
        ActivityDrawingBinding.inflate(layoutInflater)

    override fun initData() {
        preparingData()

//        val paintID = intent.getStringExtra(PAINTING_FILE_NAME)
//        if (paintID != null) {
//            preparingData(
//                fileName = paintID,
//                fillSVG = cvtFileNameIntoFillSVG(paintID),
//                strokeSVG = cvtFileNameIntoStrokeSVG(paintID),
//            )
//        } else {
//            Toast.makeText(this, "Couldn't find the painting, please try again later.", Toast.LENGTH_SHORT).show()
//            finish()
//        }
    }

    companion object {
        const val PAINTING_FILE_NAME = "INTENT_PAINTING_FILE_NAME"
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

    private val callbackListener =
        object : DrawView.OnActionCallback {
            override fun onFillColorCallback(segmentID: Int) {
                viewModel.colorSegment(segmentID)
                handler.removeCallbacks(saveBitmapThumbnailRunnable)
                handler.postDelayed(saveBitmapThumbnailRunnable, 500L)
                // update the color picker list.
                val segment =
                    viewModel.drawingUIState.value.segmentUIState
                        .find { it.id == segmentID }
                val colorItem =
                    colorPickerAdapter.currentList.find { it.color == segment?.targetColor }
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

            override fun onViewportChangeCallback(viewPortState: ViewportState) {
                viewModel.updateScaleToShowPreview(
                    viewPortState.scale > viewPortState.originalScale,
                )
                binding.imView.updateViewport(viewPortState)

                if (abs(viewPortState.scale - viewPortState.originalScale) <= 2.0f) {
                    binding.icZoomType.setImageResource(R.drawable.ic_zoomin)
                } else {
                    binding.icZoomType.setImageResource(R.drawable.ic_zoomout)
                }
            }

            override fun onLongPressSegment(segment: SegmentUIState) {
                val colorItem =
                    colorPickerAdapter.currentList.find { item -> item.color == segment.targetColor }
                colorItem?.let {
                    viewModel.setColor(it)
                }
                if (viewModel.configUIState.value.vibratePress) {
                    binding.drawview.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
        }

    @SuppressLint("DefaultLocale")
    override fun initView() {
        setupColorPicker(binding.drawview)
        viewModel.initConfiguration()
        viewModel.saveSetting(
            getBooleanPrefs(Constants.CONFIG_PREVIEW, false),
            getBooleanPrefs(Constants.CONFIG_VIBRATION, false),
        )
        binding.drawview.setListenerCallback(callbackListener)
        binding.imView.post {
            handler.postDelayed(saveBitmapThumbnailRunnable, 500L)
        }
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
            index++
            preparingData()
        }
        binding.icSetting.setSafeOnClickListener {
            SettingDrawingBTS(
                initState =
                    getBooleanPrefs(
                        Constants.CONFIG_PREVIEW,
                        false,
                    ) to getBooleanPrefs(Constants.CONFIG_VIBRATION, false),
                onSave = {
                    setBooleanPrefs(Constants.CONFIG_PREVIEW, it.first)
                    setBooleanPrefs(Constants.CONFIG_VIBRATION, it.second)
                    viewModel.saveSetting(it.first, it.second)
                },
            ).show(
                supportFragmentManager,
                "SettingDrawingBTS",
            )
        }
        binding.icZoom.setOnClickListener {
            val currentViewport = binding.drawview.getCurrentViewportState()
            currentViewport?.let { viewport ->
                if (abs(viewport.scale - viewport.originalScale) > 2.0f) {
                    binding.drawview.setScale(viewport.originalScale)
                } else {
                    binding.drawview.setScale(10f)
                }
            }
        }
        binding.icMusic.setSafeOnClickListener {
            val bottomSheet =
                SelectMusicBottomSheet(
                    currentSelectedMusic = selectedMusic,
                ) { musicItem ->
                    selectedMusic = musicItem
                    playSelectedMusic(musicItem)
                }
            bottomSheet.show(supportFragmentManager, "SelectMusicBottomSheet")
        }
        binding.btnHint.setSafeOnClickListener {
            val firstUncoloredSegment =
                viewModel.drawingUIState.value.segmentUIState
                    .firstOrNull { !it.isColored }
            firstUncoloredSegment?.let { segment ->
                val colorItem =
                    colorPickerAdapter.currentList.find { it.color == segment.targetColor }
                colorItem?.let {
                    viewModel.setColor(it)
                }
                binding.drawview.showHint()
            }
        }

        binding.btnNext.setOnClickListener {
            binding.drawview.showNextHint()
            index++
            binding.tvSegment.text = "${index}/${data.size}"
        }
        binding.btnPrev.setOnClickListener {
            binding.tvSegment.text = "${index}/${data.size}"
            index++
            binding.drawview.showPrevHint()
        }
    }

    private fun playSelectedMusic(musicItem: MusicItem?) {
        musicPlayer?.release()
        musicPlayer = null

        if (musicItem == null) {
            return
        }

        try {
            musicPlayer =
                if (musicItem.resourceId != null) {
                    MediaPlayer.create(this, musicItem.resourceId!!).apply {
                        isLooping = true
                        setOnCompletionListener {
                            release()
                            musicPlayer = null
                        }
                        start()
                    }
                } else {
                    null
                }
        } catch (e: Exception) {
            musicPlayer?.release()
            musicPlayer = null
            Toast.makeText(this, "Failed to play music", Toast.LENGTH_SHORT).show()
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
        binding.drawview.removeListenerCallback()
        musicPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer?.release()
        musicPlayer = null
    }

    override fun onResume() {
        super.onResume()
        if (selectedMusic != null && musicPlayer != null && !musicPlayer!!.isPlaying) {
            musicPlayer?.start()
        }
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
                    if (it.showPreview && it.shouldShowPreviewDueScaling) {
                        binding.imView.initBitmap(viewModel.svgFileName + ".png")
                        binding.previewView.visibility = View.VISIBLE
                    } else {
                        binding.previewView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun updatePreviewBitmap() {
        binding.imView.initBitmap(viewModel.svgFileName + ".png")
    }

    private fun preparingData(
        fileName: String,
        strokeSVG: String,
        fillSVG: String,
    ) {
        try {
            val strokePicture = fileHelper.parseCacheFileToPicture(strokeSVG)
            viewModel.initSegmentDraw(this@DrawingActivity, fileName, fillSVG, strokePicture)
            binding.drawview.loadStrokeSvgFromResource(strokePicture)
        } catch (e: Exception) {
        }
    }

    var index = 0
    val data = listOf(
        "a1.svg",
        "a2.svg",
        "a3.svg",
        "a4.svg",
        "a5.svg",
        "a6.svg",
        "a7.svg",
        "a8.svg",
        "a9.svg",
        "a10.svg",
    )

    private fun preparingData() {
        try {
            val strokePicture = Picture()
            viewModel.initSegmentDraw(
                this@DrawingActivity,
                "asdas",
                data[index % data.size],
                strokePicture
            )
//            binding.drawview.loadStrokeSvgFromResource(strokePicture)
        } catch (e: Exception) {
        }
    }
}
