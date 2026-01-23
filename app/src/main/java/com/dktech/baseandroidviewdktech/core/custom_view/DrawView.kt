package com.dktech.baseandroidviewdktech.core.custom_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withMatrix
import com.caverock.androidsvg.SVG
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import com.dktech.baseandroidviewdktech.svgparser.model.Segments
import java.io.InputStream

class DrawView
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
        var onFillColorCallback: ((Int) -> Unit)? = null

        private val segmentUIStates = mutableListOf<SegmentUIState>()
        private var selectedColor: Int = Color.RED
        private var selectedOriginalColor: Int? = null
        private var selectedLayerNumber: Int = -1

        private var overlayBitmap: Bitmap? = null
        private val bitmapPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = false
                isDither = false
            }
        private var svgBounds = RectF()
        private val overlayMatrix = Matrix()

        private var strokeSvgPicture: Picture? = null
        private var strokePngBitmap: Bitmap? = null
        private val strokeMatrix = Matrix()

        private val viewMatrix = Matrix()
        private val inverseMatrix = Matrix()

        private var lastFocusX = 0f
        private var lastFocusY = 0f

        private val drawnLayerNumbers = mutableSetOf<Int>()
        private val visibleBounds = Rect()
        private var isGestureInProgress = false
        private val textMeasurementCache = mutableMapOf<String, Float>()

        private var cachedCanvasBitmap: Bitmap? = null
        private var cachedCanvasBitmapCanvas: Canvas? = null
        private var isCacheValid = false

        private val scaleDetector: ScaleGestureDetector by lazy {
            ScaleGestureDetector(
                context,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                        isGestureInProgress = true
                        setLayerType(LAYER_TYPE_HARDWARE, null)
                        lastFocusX = detector.focusX
                        lastFocusY = detector.focusY
                        return true
                    }

                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val scaleFactor = detector.scaleFactor

                        val focusX = detector.focusX
                        val focusY = detector.focusY

                        viewMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)

                        val values = FloatArray(9)
                        viewMatrix.getValues(values)
                        val currentScale = values[Matrix.MSCALE_X]

                        if (currentScale < 0.5f || currentScale > 20f) {
                            viewMatrix.postScale(1f / scaleFactor, 1f / scaleFactor, focusX, focusY)
                        }

                        lastFocusX = focusX
                        lastFocusY = focusY
                        invalidate()
                        return true
                    }

                    override fun onScaleEnd(detector: ScaleGestureDetector) {
                        isGestureInProgress = false
                        setLayerType(LAYER_TYPE_NONE, null)
                        invalidate()
                    }
                },
            )
        }

        fun setCallbackOnColor(a: (Int) -> Unit) {
            this.onFillColorCallback = a
        }

        private val gestureDetector: GestureDetector by lazy {
            GestureDetector(
                context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent): Boolean {
                        if (!scaleDetector.isInProgress) {
                            isGestureInProgress = true
                            setLayerType(LAYER_TYPE_HARDWARE, null)
                        }
                        return true
                    }

                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float,
                    ): Boolean {
                        viewMatrix.postTranslate(-distanceX, -distanceY)
                        invalidate()
                        return true
                    }
                },
            )
        }

        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.WHITE
            }

        val gridPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.BLUE
                strokeWidth = 1f
            }

        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.BLACK
                textAlign = Paint.Align.CENTER
                textSize = 24f
            }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        fun initSegmentFile(
            svgWidth: Float,
            svgHeight: Float,
            segments: List<SegmentUIState>,
        ) {
            segmentUIStates.clear()
            segmentUIStates.addAll(segments)
            svgBounds.set(0f, 0f, svgWidth, svgHeight)
            updateOverlayMatrix()
            invalidateCache()
            invalidate()
        }

        fun invalidateCache() {
            isCacheValid = false
        }

        private fun ensureCacheBitmap() {
            if (svgBounds.isEmpty) return

            val width = svgBounds.width().toInt()
            val height = svgBounds.height().toInt()

            if (cachedCanvasBitmap == null ||
                cachedCanvasBitmap?.width != width ||
                cachedCanvasBitmap?.height != height
            ) {
                cachedCanvasBitmap?.recycle()
                cachedCanvasBitmap = createBitmap(width, height)
                cachedCanvasBitmapCanvas = Canvas(cachedCanvasBitmap!!)
                isCacheValid = false
            }
        }

        private fun updateCacheBitmap() {
            ensureCacheBitmap()

            val bitmap = cachedCanvasBitmap ?: return
            val cacheCanvas = cachedCanvasBitmapCanvas ?: return

            bitmap.eraseColor(Color.TRANSPARENT)

            segmentUIStates.forEach { uiState ->
                fillPaint.color = if (!uiState.isColored) Color.WHITE else uiState.targetColor
                cacheCanvas.drawPath(uiState.segment.path, fillPaint)
            }

            overlayBitmap?.let { overlay ->
                cacheCanvas.drawBitmap(overlay, overlayMatrix, bitmapPaint)
            }

            strokeSvgPicture?.let { picture ->
                cacheCanvas.save()
                cacheCanvas.concat(strokeMatrix)
                cacheCanvas.drawPicture(picture)
                cacheCanvas.restore()
            }

            isCacheValid = true
        }

        private fun updateOverlayMatrix() {
            val bitmap = overlayBitmap ?: return
            if (svgBounds.isEmpty) return

            val svgWidth = svgBounds.width()
            val svgHeight = svgBounds.height()

            Log.d("DrawView", "High-res PNG: ${bitmap.width}x${bitmap.height}")
            Log.d("DrawView", "Target SVG bounds: ${svgWidth}x$svgHeight")

            val scaleX = svgWidth / bitmap.width
            val scaleY = svgHeight / bitmap.height

            overlayMatrix.reset()
            overlayMatrix.postScale(scaleX, scaleY)
            overlayMatrix.postTranslate(svgBounds.left, svgBounds.top)

            Log.d(
                "DrawView",
                "Matrix scale: ${scaleX}x$scaleY (preserving ${bitmap.width}x${bitmap.height} quality)",
            )
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
        }

        fun clearStrokeOverlay() {
            strokeSvgPicture = null
            strokePngBitmap?.recycle()
            strokePngBitmap = null
            invalidate()
        }

        fun setSelectedColor(color: Int) {
            selectedColor = color
            selectedOriginalColor =
                segmentUIStates
                    .firstOrNull { it.segment.originalColor == color }
                    ?.segment
                    ?.originalColor
            updateSelectedLayerNumber()
            invalidate()
        }

        private fun updateSelectedLayerNumber() {
            selectedLayerNumber = segmentUIStates
                .firstOrNull { it.segment.originalColor == selectedColor }
                ?.layerNumber ?: -1
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val scaleHandled = scaleDetector.onTouchEvent(event)
            val gestureHandled = gestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isGestureInProgress) {
                        isGestureInProgress = false
                        setLayerType(LAYER_TYPE_NONE, null)
                        invalidate()
                    }
                }

                MotionEvent.ACTION_DOWN -> {
                    if (!scaleDetector.isInProgress) {
                        viewMatrix.invert(inverseMatrix)

                        val pts = floatArrayOf(event.x, event.y)
                        inverseMatrix.mapPoints(pts)

                        val x = pts[0].toInt()
                        val y = pts[1].toInt()

                        segmentUIStates.reversed().forEach { uiState ->
                            if (uiState.segment.region.contains(x, y) && !uiState.isColored) {
                                if (selectedOriginalColor != null && uiState.segment.originalColor == selectedOriginalColor) {
                                    onFillColorCallback?.invoke(uiState.id)
                                }
                            }
                            return@forEach
                        }
                    }
                    return true
                }
            }
            return scaleHandled || gestureHandled || super.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            drawnLayerNumbers.clear()

            if (isGestureInProgress) {
                if (!isCacheValid) {
                    updateCacheBitmap()
                }
                cachedCanvasBitmap?.let { bitmap ->
                    canvas.withMatrix(viewMatrix) {
                        drawBitmap(bitmap, 0f, 0f, bitmapPaint)
                    }
                }
            } else {
                updateVisibleBounds()
                canvas.withMatrix(viewMatrix) {
                    segmentUIStates.forEach { uiState ->
                        fillPaint.color = if (!uiState.isColored) Color.WHITE else uiState.targetColor
                        drawPath(uiState.segment.path, fillPaint)
                        if (selectedLayerNumber > 0 &&
                            uiState.layerNumber == selectedLayerNumber &&
                            !uiState.isColored
                        ) {
                            drawGridOverlay(this, uiState.segment)
                        }
                        if (shouldShowLayerNumber(uiState)) {
                            drawLayerNumber(this, uiState)
                        }
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
                }
            }
        }

        private fun updateVisibleBounds() {
            visibleBounds.set(0, 0, width, height)
            val tempMatrix = Matrix()
            viewMatrix.invert(tempMatrix)
            tempMatrix.mapRect(RectF(visibleBounds))
        }

        private fun isSegmentVisible(bounds: Rect): Boolean = Rect.intersects(visibleBounds, bounds)

        private fun drawGridOverlay(
            canvas: Canvas,
            segment: Segments,
        ) {
            val bounds = segment.region.bounds
            val gridSize = 5f
            canvas.save()
            canvas.clipPath(segment.path)

            var x = bounds.left.toFloat()
            while (x <= bounds.right) {
                canvas.drawLine(x, bounds.top.toFloat(), x, bounds.bottom.toFloat(), gridPaint)
                x += gridSize
            }

            var y = bounds.top.toFloat()
            while (y <= bounds.bottom) {
                canvas.drawLine(bounds.left.toFloat(), y, bounds.right.toFloat(), y, gridPaint)
                y += gridSize
            }

            canvas.restore()
        }

        private fun shouldShowLayerNumber(uiState: SegmentUIState): Boolean {
            val segment = uiState.segment
            val bounds = segment.region.bounds
            val textSize = bounds.width().coerceAtMost(bounds.height()) / 3f

            val values = FloatArray(9)
            viewMatrix.getValues(values)
            val currentScale = values[Matrix.MSCALE_X]
            val scaledTextSize = textSize * currentScale

            val minTextSize = 12f
            if (scaledTextSize < minTextSize) {
                return false
            }

            val text = uiState.layerNumber.toString()
            val cacheKey = "$text-$textSize"
            val textWidth =
                textMeasurementCache.getOrPut(cacheKey) {
                    textPaint.textSize = textSize
                    textPaint.measureText(text)
                } * currentScale

            textPaint.textSize = textSize
            val textHeight = (textPaint.descent() - textPaint.ascent()) * currentScale

            val scaledWidth = bounds.width() * currentScale
            val scaledHeight = bounds.height() * currentScale

            return textWidth <= scaledWidth * 0.8f && textHeight <= scaledHeight * 0.8f
        }

        private fun drawLayerNumber(
            canvas: Canvas,
            uiState: SegmentUIState,
        ) {
            val segment = uiState.segment
            val bounds = segment.region.bounds
            val textSize = bounds.width().coerceAtMost(bounds.height()) / 4f
            textPaint.textSize = textSize

            val text = uiState.layerNumber.toString()
            val x = bounds.centerX().toFloat()
            val y = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

            canvas.save()
            canvas.clipPath(segment.path)
            canvas.drawText(text, x, y, textPaint)
            canvas.restore()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            cachedCanvasBitmap?.recycle()
            cachedCanvasBitmap = null
            cachedCanvasBitmapCanvas = null
        }
    }
