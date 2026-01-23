package com.dktech.baseandroidviewdktech.core.custom_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withMatrix
import com.dktech.baseandroidviewdktech.model.SegmentUIState

class CustomImageView
@JvmOverloads constructor(
    mContext: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0,
) : View(
    mContext,
    attr,
    defStyle,
) {

    private val segmentUIStates = mutableListOf<SegmentUIState>()
    
    private var overlayBitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = false
        isDither = false
    }
    
    private var svgBounds = RectF()
    private val overlayMatrix = Matrix()

    private var strokeSvgPicture: Picture? = null
    private var strokePngBitmap: Bitmap? = null
    private val strokeMatrix = Matrix()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    fun initSegmentFile(svgWidth: Float, svgHeight: Float, segments: List<SegmentUIState>) {
        segmentUIStates.clear()
        segmentUIStates.addAll(segments)
        svgBounds.set(0f, 0f, svgWidth, svgHeight)
        updateOverlayMatrix()
        updateStrokeMatrix()
        invalidate()
    }

    private fun updateOverlayMatrix() {
        val bitmap = overlayBitmap ?: return
        if (svgBounds.isEmpty) return

        val svgWidth = svgBounds.width()
        val svgHeight = svgBounds.height()

        val scaleX = svgWidth / bitmap.width
        val scaleY = svgHeight / bitmap.height

        overlayMatrix.reset()
        overlayMatrix.postScale(scaleX, scaleY)
        overlayMatrix.postTranslate(svgBounds.left, svgBounds.top)
    }

    fun loadOverlayBitmap(bitmap: Bitmap) {
        overlayBitmap?.recycle()
        overlayBitmap = bitmap
        updateOverlayMatrix()
        invalidate()
    }

    fun clearOverlayBitmap() {
        overlayBitmap?.recycle()
        overlayBitmap = null
        invalidate()
    }

    fun loadStrokeSvgFromResource(asset: Picture) {
        try {
            strokeSvgPicture = asset
            updateStrokeMatrix()
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadStrokePngFromResource(bitmap: Bitmap) {
        try {
            strokePngBitmap?.recycle()
            strokePngBitmap = bitmap
            updateStrokeMatrix()
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStrokeMatrix() {
        if (svgBounds.isEmpty) return

        val svgWidth = svgBounds.width()
        val svgHeight = svgBounds.height()

        strokeSvgPicture?.let { picture ->
            val scaleX = svgWidth / picture.width
            val scaleY = svgHeight / picture.height
            strokeMatrix.reset()
            strokeMatrix.postScale(scaleX, scaleY)
            strokeMatrix.postTranslate(svgBounds.left, svgBounds.top)
        }

        strokePngBitmap?.let { bitmap ->
            val scaleX = svgWidth / bitmap.width
            val scaleY = svgHeight / bitmap.height
            strokeMatrix.reset()
            strokeMatrix.postScale(scaleX, scaleY)
            strokeMatrix.postTranslate(svgBounds.left, svgBounds.top)
        }
    }

    fun clearStrokeOverlay() {
        strokeSvgPicture = null
        strokePngBitmap?.recycle()
        strokePngBitmap = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        segmentUIStates.forEach { uiState ->
            fillPaint.color = if (!uiState.isColored) Color.WHITE else uiState.targetColor
            canvas.drawPath(uiState.segment.path, fillPaint)
        }

        overlayBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, overlayMatrix, bitmapPaint)
        }

        strokeSvgPicture?.let { picture ->
            canvas.save()
            canvas.concat(strokeMatrix)
            canvas.drawPicture(picture)
            canvas.restore()
        }

        strokePngBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, strokeMatrix, bitmapPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        overlayBitmap?.recycle()
        overlayBitmap = null
        strokePngBitmap?.recycle()
        strokePngBitmap = null
    }
}