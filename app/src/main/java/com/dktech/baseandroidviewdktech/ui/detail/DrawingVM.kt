package com.dktech.baseandroidviewdktech.ui.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Picture
import android.util.Log
import androidx.collection.IntIntMap
import androidx.collection.MutableIntIntMap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withMatrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.local.model.ColoredSegment
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import com.dktech.baseandroidviewdktech.svgparser.SegmentLoadState
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.svgparser.model.Segments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.forEach

data class DrawingUIState(
    val svgWidth: Float = 0f,
    val svgHeight: Float = 0f,
    val color: List<ColorItem> = emptyList(),
    val segmentUIState: List<SegmentUIState> = emptyList(),
)

data class ConfigUIState(
    val showPreview: Boolean = false,
    val shouldShowPreviewDueScaling: Boolean = false,
    val vibratePress: Boolean = false,
    val currentSelectedColor: ColorItem? = null,
)

class DrawingVM : ViewModel() {
    private var _drawingUIState = MutableStateFlow<DrawingUIState>(DrawingUIState())
    val drawingUIState = _drawingUIState.asStateFlow()

    private var svgFileName: String = ""
    private var svgStrokeFileName: Picture? = null

    private var _configUIState = MutableStateFlow<ConfigUIState>(ConfigUIState())
    val configUIState = _configUIState.asStateFlow()

    private val segmentParser by lazy {
        SegmentParser()
    }

    private lateinit var segmentLoadState: SegmentLoadState

    fun updateScaleToShowPreview(value: Boolean){
        _configUIState.value =
            _configUIState.value.copy(
                shouldShowPreviewDueScaling = value
            )
    }

    fun saveSetting(preview: Boolean, vibrate: Boolean){
        _configUIState.value =
            _configUIState.value.copy(
                showPreview = preview, vibratePress = vibrate
            )
    }

    fun setColor(colorItem: ColorItem) {
        _configUIState.value =
            _configUIState.value.copy(
                currentSelectedColor = colorItem,
            )
    }

    fun initConfiguration() {
    }

    fun colorSegment(segmentID: Int) {
        if (!::segmentLoadState.isInitialized) {
            return
        }
        updateSegmentColored(segmentID)
        // update color state

        viewModelScope.launch {
            segmentLoadState.segmentColoredStateDB.insertColoredSegment(
                ColoredSegment(
                    fileName = svgFileName,
                    segmentId = segmentID,
                ),
            )
        }
    }

    // update segment color state
    private fun updateSegmentColored(segmentID: Int) {
        val segmentIndex = _drawingUIState.value.segmentUIState.indexOfFirst { it.id == segmentID }
        if (segmentIndex != -1) {
            val segment =
                _drawingUIState.value.segmentUIState[segmentIndex].copy(
                    isColored = true,
                )
            val newList = _drawingUIState.value.segmentUIState.toMutableList()
            newList[segmentIndex] = segment
            _drawingUIState.value =
                _drawingUIState.value.copy(
                    segmentUIState = newList,
                )
        }
    }

    /*
     - {fileName} is the file to distinguish different file and also use as the filled segment
     - strokePicture is the picture parsed from svg file with the help of AndroidSVG
     @Credits: anhnt
     */
    fun initSegmentDraw(
        mContext: Context,
        fileName: String,
        strokePicture: Picture,
    ) {
        if (!::segmentLoadState.isInitialized) {
            segmentLoadState =
                SegmentLoadState(
                    AppDatabase.getDatabase(mContext).colorSegmentDAO(),
                )
        }

        svgFileName = "$fileName.svg"
        svgStrokeFileName = strokePicture

        viewModelScope.launch {
            val svgFile =
                segmentParser.parseSVGFile(
                    mContext,
                    svgFileName,
                )
            var segmentsUIState = mutableListOf<SegmentUIState>()
            svgFile.paths.forEach { group ->
                group.segments.forEach { segment ->
                    segmentsUIState.add(
                        SegmentUIState(
                            id = segment.id,
                            segment,
                            targetColor = segment.originalColor ?: Color.WHITE,
                        ),
                    )
                }
            }

            segmentsUIState =
                segmentLoadState.constructStateSegment(fileName, segmentsUIState).toMutableList()

            val uniqueColor = getUniqueColors(segmentsUIState.map { it.segment })

            val mapColorToLayer = mapLayersNumber(segmentsUIState)

            segmentsUIState.forEach { segment ->
                segment.segment.originalColor?.let {
                    val layerNumber = mapColorToLayer[it]
                    val updatedSegment = segment.copy(layerNumber = layerNumber)
                    val index = segmentsUIState.indexOf(segment)
                    segmentsUIState[index] = updatedSegment
                }
            }

            val colorItems =
                uniqueColor
                    .map { color ->
                        ColorItem(
                            color,
                            mapColorToLayer[color],
                            segmentsUIState
                                .filter { !it.isColored }
                                .count { it.segment.originalColor == color },
                        )
                    }.filter { it.freqShown != 0 }

            _drawingUIState.value =
                _drawingUIState.value.copy(
                    svgWidth = svgFile.width.toFloat(),
                    svgHeight = svgFile.height.toFloat(),
                    color = colorItems,
                    segmentUIState = segmentsUIState,
                )
        }
    }

    private fun getUniqueColors(segment: List<Segments>): List<Int> =
        segment
            .mapNotNull { it.originalColor }
            .distinct()

    private fun mapLayersNumber(segments: List<SegmentUIState>): IntIntMap {
        val colorToLayerMap = MutableIntIntMap()
        var currentLayer = 1
        segments.forEach { segment ->
            val color = segment.segment.originalColor
            color?.let {
                if (!colorToLayerMap.containsKey(color)) {
                    colorToLayerMap[color] = currentLayer++
                }
            }
        }
        return colorToLayerMap
    }

    // Have to ensure the job is finished even if the viewmodel is cleared. -_-
    private var unLifeScopedCoroutine = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun onScreenLeaving(cacheDir: File, onSuccess: () -> Unit) {
        unLifeScopedCoroutine.launch {
            saveCacheThumbnail(File(cacheDir, svgFileName.replace("svg", "png")))
            withContext(Dispatchers.Main.immediate){
                onSuccess()
            }
        }
    }

    private suspend fun saveCacheThumbnail(fileOutputDir: File) {
        withTimeout(10000L) {
            runInterruptible {
                val thumbnailSize = 512
                val segments = _drawingUIState.value.segmentUIState
                val svgWidth = _drawingUIState.value.svgWidth
                val svgHeight = _drawingUIState.value.svgHeight

                if (segments.isEmpty() || svgWidth <= 0 || svgHeight <= 0) return@runInterruptible

                val bitmap = createBitmap(thumbnailSize, thumbnailSize)
                val canvas = Canvas(bitmap)

                val scaleX = thumbnailSize / svgWidth
                val scaleY = thumbnailSize / svgHeight
                val scale = minOf(scaleX, scaleY)

                val scaledWidth = svgWidth * scale
                val scaledHeight = svgHeight * scale
                val offsetX = (thumbnailSize - scaledWidth) / 2f
                val offsetY = (thumbnailSize - scaledHeight) / 2f

                val drawMatrix =
                    Matrix().apply {
                        postScale(scale, scale)
                        postTranslate(offsetX, offsetY)
                    }

                val fillPaint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                    }

                canvas.drawColor(Color.WHITE)

                canvas.withMatrix(drawMatrix) {
                    segments.forEach { uiState ->
                        fillPaint.color = if (!uiState.isColored) Color.WHITE else uiState.targetColor
                        drawPath(uiState.segment.path, fillPaint)
                    }

                    svgStrokeFileName?.let {
                        val strokeMatrix =
                            Matrix().apply {
                                val strokeScaleX = svgWidth / it.width
                                val strokeScaleY = svgHeight / it.height
                                postScale(strokeScaleX, strokeScaleY)
                            }
                        withMatrix(strokeMatrix) {
                            drawPicture(it)
                        }
                    }
                }

                try {
                    FileOutputStream(fileOutputDir).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    bitmap.recycle()
                }
            }
        }
    }
}
