package com.dktech.baseandroidviewdktech.ui.detail

import android.content.Context
import android.graphics.Color
import androidx.collection.IntIntMap
import androidx.collection.MutableIntIntMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.local.model.ColoredSegment
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import com.dktech.baseandroidviewdktech.svgparser.SegmentLoadState
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.svgparser.model.Segments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.forEach

data class DrawingUIState(
    val svgWidth: Float = 0f,
    val svgHeight: Float = 0f,
    val color: List<ColorItem> = emptyList(),
    val segmentUIState: List<SegmentUIState> = emptyList(),
)

data class ConfigUIState(
    val showPrevent: Boolean = false,
    val vibratePress: Boolean = false,
    val currentSelectedColor: ColorItem? = null,
)

class DrawingVM : ViewModel() {
    private var _drawingUIState = MutableStateFlow<DrawingUIState>(DrawingUIState())
    val drawingUIState = _drawingUIState.asStateFlow()

    private var _configUIState = MutableStateFlow<ConfigUIState>(ConfigUIState())
    val configUIState = _configUIState.asStateFlow()

    private val segmentParser by lazy {
        SegmentParser()
    }

    private lateinit var segmentLoadState: SegmentLoadState

    fun setColor(colorItem: ColorItem) {
        _configUIState.value =
            _configUIState.value.copy(
                currentSelectedColor = colorItem,
            )
    }

    fun initConfiguration() {
    }

    private var currentFileName: String = ""

    fun colorSegment(segmentID: Int) {
        if (!::segmentLoadState.isInitialized) {
            return
        }

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
        viewModelScope.launch {
            segmentLoadState.segmentColoredStateDB.insertColoredSegment(
                ColoredSegment(
                    fileName = currentFileName,
                    segmentId = segmentID,
                ),
            )
        }
    }

    fun initSegmentDraw(
        mContext: Context,
        fileName: String,
    ) {
        if (!::segmentLoadState.isInitialized) {
            segmentLoadState =
                SegmentLoadState(
                    AppDatabase.getDatabase(mContext).colorSegmentDAO(),
                )
        }

        currentFileName = fileName
        viewModelScope.launch {
            val svgFile =
                segmentParser.parseSVGFile(
                    mContext,
                    currentFileName,
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
}
