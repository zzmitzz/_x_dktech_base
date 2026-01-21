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
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withMatrix
import com.dktech.baseandroidviewdktech.svgparser.SVGInfo
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import java.io.File

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
        private var segmentFiles: SVGInfo? = null
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

        private val debugPaint =
            Paint().apply {
                style = Paint.Style.FILL
                alpha = 80
            }

        private val viewMatrix = Matrix()
        private val inverseMatrix = Matrix()

        private var lastFocusX = 0f
        private var lastFocusY = 0f

        private val drawnLayerNumbers = mutableSetOf<Int>()
        private val visibleBounds = RectF()
        private var isGestureInProgress = false
        private val textMeasurementCache = mutableMapOf<String, Float>()

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

        fun initSegmentFile(segmentFile: SVGInfo) {
            segmentFiles = segmentFile
            segmentUIStates.clear()

            svgBounds.set(0f, 0f, segmentFile.width.toFloat(), segmentFile.height.toFloat())

            segmentFile.paths.forEach { group ->
                group.segments.forEach { segment ->
                    segmentUIStates.add(
                        SegmentUIState(
                            segment,
                            fillColor = if (segment.originalColor != null) Color.WHITE else Color.BLACK,
                        ),
                    )
                }
            }

            updateOverlayMatrix()
            invalidate()
        }

        fun loadOverlayBitmapFromFile(filePath: String) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    overlayBitmap?.recycle()

                    val options =
                        BitmapFactory.Options().apply {
                            inScaled = false
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                    overlayBitmap = BitmapFactory.decodeFile(filePath, options)
                    updateOverlayMatrix()
                    invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun loadOverlayBitmapFromResource(resourceId: Int) {
            try {
                overlayBitmap?.recycle()

                val options =
                    BitmapFactory.Options().apply {
                        inScaled = false
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                overlayBitmap = BitmapFactory.decodeResource(resources, resourceId, options)
                updateOverlayMatrix()
                invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

            Log.d("DrawView", "Matrix scale: ${scaleX}x$scaleY (preserving ${bitmap.width}x${bitmap.height} quality)")
        }

        fun clearOverlayBitmap() {
            overlayBitmap?.recycle()
            overlayBitmap = null
            invalidate()
        }

        fun getUniqueColors(): List<Int> =
            segmentUIStates
                .mapNotNull { it.segment.originalColor }
                .distinct()
                .sorted()

        fun getColorToLayerMap(): Map<Int, Int> {
            val colorToLayerMap = mutableMapOf<Int, Int>()
            segmentUIStates.forEach { uiState ->
                uiState.segment.originalColor?.let { color ->
                    if (!colorToLayerMap.containsKey(color)) {
                        colorToLayerMap[color] = uiState.segment.layerNumber
                    }
                }
            }
            return colorToLayerMap
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
                ?.segment
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
                            if (uiState.segment.region.contains(x, y)) {
                                if (selectedOriginalColor != null && uiState.segment.originalColor == selectedOriginalColor) {
                                    uiState.fillColor = selectedColor
                                    invalidate()
                                    return true
                                } else if (selectedOriginalColor == null) {
                                    uiState.fillColor = uiState.segment.originalColor ?: Color.BLACK
                                    invalidate()
                                    return true
                                }
                                return true
                            }
                        }
                    }
                }
            }
            return scaleHandled || gestureHandled || super.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawnLayerNumbers.clear()

            updateVisibleBounds()

            canvas.withMatrix(viewMatrix) {
                segmentUIStates.forEach { uiState ->
                    if (!isGestureInProgress || isSegmentVisible(uiState.segment.bounds)) {
                        fillPaint.color = uiState.fillColor
                        drawPath(uiState.segment.path, fillPaint)
                        if (!isGestureInProgress) {
                            if (selectedLayerNumber > 0 &&
                                uiState.segment.layerNumber == selectedLayerNumber &&
                                uiState.fillColor == Color.WHITE
                            ) {
                                drawGridOverlay(this, uiState.segment)
                            }

                            if (shouldShowLayerNumber(uiState.segment)) {
                                drawLayerNumber(this, uiState.segment)
                            }
                        }
                    }
                }

                overlayBitmap?.let { bitmap ->
                    canvas.drawBitmap(bitmap, overlayMatrix, bitmapPaint)
                }
            }
        }

        private fun updateVisibleBounds() {
            visibleBounds.set(0f, 0f, width.toFloat(), height.toFloat())
            val tempMatrix = Matrix()
            viewMatrix.invert(tempMatrix)
            tempMatrix.mapRect(visibleBounds)
        }

        private fun isSegmentVisible(bounds: RectF): Boolean = RectF.intersects(visibleBounds, bounds)

        private fun drawGridOverlay(
            canvas: Canvas,
            segment: com.dktech.baseandroidviewdktech.svgparser.Segments,
        ) {
            val bounds = segment.bounds
            val gridSize = 5f

            canvas.save()
            canvas.clipPath(segment.path)

            var x = bounds.left
            while (x <= bounds.right) {
                canvas.drawLine(x, bounds.top, x, bounds.bottom, gridPaint)
                x += gridSize
            }

            var y = bounds.top
            while (y <= bounds.bottom) {
                canvas.drawLine(bounds.left, y, bounds.right, y, gridPaint)
                y += gridSize
            }

            canvas.restore()
        }

        private fun shouldShowLayerNumber(segment: com.dktech.baseandroidviewdktech.svgparser.Segments): Boolean {
            val bounds = segment.bounds
            val textSize = bounds.width().coerceAtMost(bounds.height()) / 3f

            val values = FloatArray(9)
            viewMatrix.getValues(values)
            val currentScale = values[Matrix.MSCALE_X]
            val scaledTextSize = textSize * currentScale

            val minTextSize = 12f
            if (scaledTextSize < minTextSize) {
                return false
            }

            val text = segment.layerNumber.toString()
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
            segment: com.dktech.baseandroidviewdktech.svgparser.Segments,
        ) {
            val bounds = segment.bounds
            val textSize = bounds.width().coerceAtMost(bounds.height()) / 4f
            textPaint.textSize = textSize

            val text = segment.layerNumber.toString()
            val x = bounds.centerX()
            val y = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

            canvas.save()
            canvas.clipPath(segment.path)
            canvas.drawText(text, x, y, textPaint)
            canvas.restore()
        }
    }
