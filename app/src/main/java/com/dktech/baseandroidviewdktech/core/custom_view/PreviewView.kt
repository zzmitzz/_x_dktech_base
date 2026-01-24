package com.dktech.baseandroidviewdktech.core.custom_view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class PreviewView
    @JvmOverloads
    constructor(
        mContext: Context,
        attr: AttributeSet? = null,
        defStyle: Int = 0,
    ) : View(
            mContext,
            attr,
            defStyle,
        ) {
        private val bitmapPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = false
                isDither = false
            }
        private val framePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
            }
    }
