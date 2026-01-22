package com.dktech.baseandroidviewdktech.ui.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.RawRes
import androidx.collection.IntIntMap
import androidx.collection.MutableIntIntMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.model.ColorItem
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import com.dktech.baseandroidviewdktech.svgparser.SVGInfo
import com.dktech.baseandroidviewdktech.svgparser.SegmentGroup
import com.dktech.baseandroidviewdktech.svgparser.SegmentParser
import com.dktech.baseandroidviewdktech.svgparser.Segments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.containsKey
import kotlin.collections.forEach
import kotlin.collections.get


data class DrawingUIState(
    val svgWidth: Float = 0f,
    val svgHeight: Float = 0f,
    val color: List<ColorItem> = emptyList(),
    val segmentUIState: List<SegmentUIState> = emptyList()
)

data class ConfigUIState(
    val showPrevent: Boolean = false,
    val vibratePress: Boolean = false,
    val currentSelectedColor: ColorItem? = null
)


class DrawingVM : ViewModel() {

    private var _drawingUIState = MutableStateFlow<DrawingUIState>(DrawingUIState())
    val drawingUIState = _drawingUIState.asStateFlow()

    var totalSegments: Int = 1
    var coloredSegment: Int = 0

    private var _configUIState = MutableStateFlow<ConfigUIState>(ConfigUIState())
    val configUIState = _configUIState.asStateFlow()

    private val segmentParser by lazy {
        SegmentParser()
    }

    fun setColor(colorItem: ColorItem){
        _configUIState.value = _configUIState.value.copy(
            currentSelectedColor = colorItem
        )
    }

    fun initConfiguration(){

    }


    fun initSegmentDraw(mContext: Context, @RawRes file: Int) {
        viewModelScope.launch() {
            val svgFile =
                segmentParser.parseSVGFile(
                    mContext,
                    file,
                )
            val segmentsUIState = mutableListOf<SegmentUIState>()
            svgFile.paths.forEach { group ->
                group.segments.forEach { segment ->
                    segmentsUIState.add(
                        SegmentUIState(
                            segment,
                            fillColor = if (segment.originalColor != null) Color.WHITE else Color.BLACK,
                        ),
                    )
                }
            }
            totalSegments = segmentsUIState.size
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
                uniqueColor.map { color ->
                    ColorItem(
                        color,
                        mapColorToLayer[color],
                        segmentsUIState.count { it.segment.originalColor == color })
                }

            _drawingUIState.value = _drawingUIState.value.copy(
                svgWidth = svgFile.width.toFloat(),
                svgHeight = svgFile.height.toFloat(),
                color = colorItems,
                segmentUIState = segmentsUIState
            )
        }
    }
    fun incColored(){
        coloredSegment++
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
                if(!colorToLayerMap.containsKey(color)){
                    colorToLayerMap[color] = currentLayer++
                }
            }
        }
        return colorToLayerMap
    }

}