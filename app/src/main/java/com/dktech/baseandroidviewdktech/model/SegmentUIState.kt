package com.dktech.baseandroidviewdktech.model

import android.graphics.Color
import android.graphics.Paint
import com.dktech.baseandroidviewdktech.svgparser.Segments

data class SegmentUIState(
    val segment: Segments,
    var fillColor: Int = Color.WHITE,
    var isColored: Boolean = false,
    var layerNumber: Int = -1,
)
