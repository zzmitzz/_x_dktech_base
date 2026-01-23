package com.dktech.baseandroidviewdktech.model

import android.graphics.Color
import com.dktech.baseandroidviewdktech.svgparser.model.Segments

data class SegmentUIState(
    val id: Int,
    val segment: Segments,
    var targetColor: Int = Color.WHITE,
    var isColored: Boolean = false,
    var layerNumber: Int = -1,
)
