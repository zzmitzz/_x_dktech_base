package com.dktech.baseandroidviewdktech.svgparser

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region

data class Segments(
    val id: String,
    val number: Int,
    val path: Path,
    val region: Region,
    val fillPaint: Paint,
    val strokePaint: Paint?,
    val originalColor: Int?,
    val bounds: RectF = RectF(),
    val layerNumber: Int = 1,
)
