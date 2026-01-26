package com.dktech.baseandroidviewdktech.core.custom_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.io.File

class PreviewView
@JvmOverloads
constructor(
    val mContext: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0,
) : View(
    mContext,
    attr,
    defStyle,
) {

    init {
        isClickable = false
        isFocusable = false
    }

    private var bitmap: Bitmap? = null
    private var viewportRect = RectF()

    fun initBitmap(fileName: String) {
        try {
            val file = File(mContext.cacheDir, fileName).inputStream()
            bitmap = BitmapFactory.decodeStream(file)
            file.close()
            updateMatrix()
            invalidate()
        }catch (e: Exception){

        }
    }

    private val bitmapPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = false
            isDither = false
        }

    private val viewportFillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(50, 33, 150, 243)
        }

    private val viewportStrokePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(200, 33, 150, 243)
            strokeWidth = 2f
        }

    private val matrix = Matrix()

    private fun updateMatrix() {
        val bmp = bitmap ?: return
        if (width == 0 || height == 0) return

        matrix.reset()

        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val bmpW = bmp.width.toFloat()
        val bmpH = bmp.height.toFloat()

        val scale = minOf(
            viewW / bmpW,
            viewH / bmpH
        ) * 0.9f

        val dx = (viewW - bmpW * scale) * 0.5f
        val dy = (viewH - bmpH * scale) * 0.5f

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
    }

    fun updateViewport(state: ViewportState) {
        val bmp = bitmap ?: return
        if (width == 0 || height == 0) return

        val bmpW = bmp.width.toFloat()
        val bmpH = bmp.height.toFloat()
        val viewW = width.toFloat()
        val viewH = height.toFloat()

        val previewScale = minOf(viewW / bmpW, viewH / bmpH) * 0.9f
        val previewDx = (viewW - bmpW * previewScale) * 0.5f
        val previewDy = (viewH - bmpH * previewScale) * 0.5f

        val svgToBmpScaleX = bmpW / state.svgWidth
        val svgToBmpScaleY = bmpH / state.svgHeight

        val visibleLeftInSvg = -state.translateX / state.scale
        val visibleTopInSvg = -state.translateY / state.scale
        val visibleWidthInSvg = state.viewWidth / state.scale
        val visibleHeightInSvg = state.viewHeight / state.scale

        val leftInBmp = visibleLeftInSvg * svgToBmpScaleX
        val topInBmp = visibleTopInSvg * svgToBmpScaleY
        val rightInBmp = (visibleLeftInSvg + visibleWidthInSvg) * svgToBmpScaleX
        val bottomInBmp = (visibleTopInSvg + visibleHeightInSvg) * svgToBmpScaleY

        viewportRect.set(
            previewDx + leftInBmp * previewScale,
            previewDy + topInBmp * previewScale,
            previewDx + rightInBmp * previewScale,
            previewDy + bottomInBmp * previewScale
        )

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, matrix, bitmapPaint)
        }

        if (!viewportRect.isEmpty) {
            canvas.drawRect(viewportRect, viewportFillPaint)
            canvas.drawRect(viewportRect, viewportStrokePaint)
        }
    }
}
