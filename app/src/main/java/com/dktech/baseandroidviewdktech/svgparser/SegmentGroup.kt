package com.dktech.baseandroidviewdktech.svgparser

import android.graphics.Color
import android.graphics.Matrix

data class SegmentGroup(
    val id: String,
    val name: String?,
    val transform: Matrix,
    val segments: MutableList<Segments>,
)
