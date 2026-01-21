package com.dktech.baseandroidviewdktech.utils

import android.graphics.Paint
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.dktech.baseandroidviewdktech.R


data class Painting(
    val id: Int = 1,
    @DrawableRes val overLayLinePaint: Int = R.drawable.line_paint_1,
    @RawRes val underLayerPaint: Int = R.raw.line_paint_1
)

// Damn thing live as application lifecycle
object Constants {
    val mockListData = buildList {
        repeat(10) {
            add(Painting(it))
        }
    }
}