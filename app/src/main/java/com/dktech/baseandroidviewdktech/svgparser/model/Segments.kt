package com.dktech.baseandroidviewdktech.svgparser.model

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Region

data class Segments(
    val id: Int = 0,
    val path: Path,
    val region: Region,
    val fillPaint: Paint,
    val originalColor: Int?,
)
