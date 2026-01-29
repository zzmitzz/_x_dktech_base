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
import androidx.core.graphics.withClip
import androidx.core.graphics.withMatrix
import com.caverock.androidsvg.SVG
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import com.dktech.baseandroidviewdktech.svgparser.model.Segments
import java.io.InputStream

data class ViewportState(
    val scale: Float,
    val translateX: Float,
    val translateY: Float,
    val svgWidth: Float,
    val svgHeight: Float,
    val viewWidth: Int,
    val viewHeight: Int,
    val originalScale: Float, // this flag represent for the minimum scale that should open the preview
)

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
        interface OnActionCallback {
            fun onFillColorCallback(segmentID: Int): Unit

            fun onViewportChangeCallback(viewPortState: ViewportState): Unit

            fun onLongPressSegment(segment: SegmentUIState): Unit
        }

        private var listenerComponent: OnActionCallback? = null

        private val segmentUIStates = mutableListOf<SegmentUIState>()
        private var selectedColor: Int = Color.RED
        private var selectedOriginalColor: Int? = null
        private var selectedLayerNumber: Int = -1

        private val bitmapPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = false
                isDither = false
            }
        private var svgBounds = RectF()
        private val overlayMatrix = Matrix()

        private var strokeSvgPicture: Picture? = null
        private val strokeMatrix = Matrix()

        private val viewMatrix = Matrix()
        private val inverseMatrix = Matrix()

        private var lastFocusX = 0f
        private var lastFocusY = 0f

        private val drawnLayerNumbers = mutableSetOf<Int>()
        private val visibleBounds = Rect()
        private var isGestureInProgress = false
        private val textMeasurementCache = mutableMapOf<String, Float>()
        private var currentHintIndex = -1
        private var highlightedSegmentId: Int? = null

        private var minScaleFactor = 0.5f
        private var isInitialSetupDone = false

        private var cachedCanvasBitmap: Bitmap? = null
        private var cachedCanvasBitmapCanvas: Canvas? = null
        private var isCacheValid = false

        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.WHITE
            }

        private val textureBitmap: Bitmap by lazy {
            BitmapFactory.decodeResource(
                resources,
                com.dktech.baseandroidviewdktech.R.drawable.placeholder_texture,
            )
        }

        private val gridOverlayCache = mutableMapOf<Int, Bitmap>()

        private val gridBitmapPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
            }

        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.BLACK
                textAlign = Paint.Align.CENTER
                textSize = 24f
            }

        val textStrokePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.WHITE
                strokeWidth = 1f
                textAlign = Paint.Align.CENTER
                textSize = 24f
            }

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

                        if (currentScale !in minScaleFactor..60f) {
                            viewMatrix.postScale(1f / scaleFactor, 1f / scaleFactor, focusX, focusY)
                        }

                        constrainViewMatrix()
                        notifyViewportChange()

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
                        setLayerType(LAYER_TYPE_HARDWARE, null)
//                        if (!scaleDetector.isInProgress) {
//                            viewMatrix.invert(inverseMatrix)
//
//                            val pts = floatArrayOf(e.x, e.y)
//                            inverseMatrix.mapPoints(pts)
//
//                            val x = pts[0].toInt()
//                            val y = pts[1].toInt()
//
//                            segmentUIStates.reversed().forEach { uiState ->
//                                if (uiState.segment.region.contains(x, y) && !uiState.isColored) {
//                                    if (selectedOriginalColor != null && uiState.segment.originalColor == selectedOriginalColor) {
//                                        listenerComponent?.onFillColorCallback(uiState.id)
//                                        if (highlightedSegmentId == uiState.id) {
//                                            highlightedSegmentId = null
//                                        }
//                                    }
//                                }
//                                return@forEach
//                            }
//                        }
                        return true
                    }

                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float,
                    ): Boolean {
                        if (!scaleDetector.isInProgress) {
                            isGestureInProgress = true
                        }
                        viewMatrix.postTranslate(-distanceX, -distanceY)
                        constrainViewMatrix()
                        notifyViewportChange()
                        invalidate()
                        return true
                    }

                    override fun onLongPress(e: MotionEvent) {
                        super.onLongPress(e)
                        if (!scaleDetector.isInProgress) {
                            viewMatrix.invert(inverseMatrix)

                            val pts = floatArrayOf(e.x, e.y)
                            inverseMatrix.mapPoints(pts)

                            val x = pts[0].toInt()
                            val y = pts[1].toInt()

                            segmentUIStates.reversed().forEach { uiState ->
                                if (uiState.segment.region.contains(x, y) && !uiState.isColored) {
                                    listenerComponent?.onLongPressSegment(uiState)
                                }
                                return@forEach
                            }
                        }
                    }
                },
            )
        }

        public fun setListenerCallback(callback: OnActionCallback) {
            listenerComponent = callback
        }

        public fun removeListenerCallback() {
            listenerComponent = null
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w > 0 && h > 0 && !svgBounds.isEmpty) {
                setupInitialTransform()
            }
        }

        private fun setupInitialTransform() {
            if (width <= 0 || height <= 0 || svgBounds.isEmpty) return

            val svgWidth = svgBounds.width()
            val svgHeight = svgBounds.height()

            val scaleX = width.toFloat() / svgWidth
            val scaleY = height.toFloat() / svgHeight
            val fitScale = minOf(scaleX, scaleY)

            minScaleFactor = fitScale * 0.9f

            val scaledWidth = svgWidth * fitScale
            val scaledHeight = svgHeight * fitScale
            val translateX = (width - scaledWidth) / 2f
            val translateY = (height - scaledHeight) / 2f

            viewMatrix.reset()
            viewMatrix.postScale(fitScale, fitScale)
            viewMatrix.postTranslate(translateX, translateY)

            isInitialSetupDone = true
            notifyViewportChange()
        }

        private fun constrainViewMatrix() {
            if (width <= 0 || height <= 0 || svgBounds.isEmpty) return

            val values = FloatArray(9)
            viewMatrix.getValues(values)

            val currentScale = values[Matrix.MSCALE_X]
            var translateX = values[Matrix.MTRANS_X]
            var translateY = values[Matrix.MTRANS_Y]

            val scaledSvgWidth = svgBounds.width() * currentScale
            val scaledSvgHeight = svgBounds.height() * currentScale

            val minTranslateX = width / 2f - scaledSvgWidth
            val maxTranslateX = width / 2f
            val minTranslateY = height / 2f - scaledSvgHeight
            val maxTranslateY = height / 2f

            translateX = translateX.coerceIn(minTranslateX, maxTranslateX)
            translateY = translateY.coerceIn(minTranslateY, maxTranslateY)

            values[Matrix.MTRANS_X] = translateX
            values[Matrix.MTRANS_Y] = translateY
            viewMatrix.setValues(values)
        }

        private fun notifyViewportChange() {
            if (width <= 0 || height <= 0 || svgBounds.isEmpty) return

            val values = FloatArray(9)
            viewMatrix.getValues(values)

            listenerComponent?.onViewportChangeCallback(
                ViewportState(
                    scale = values[Matrix.MSCALE_X],
                    translateX = values[Matrix.MTRANS_X],
                    translateY = values[Matrix.MTRANS_Y],
                    svgWidth = svgBounds.width(),
                    svgHeight = svgBounds.height(),
                    viewWidth = width,
                    viewHeight = height,
                    originalScale = minScaleFactor / 0.9f,
                ),
            )
        }

        fun setScale(targetScale: Float) {
            if (width <= 0 || height <= 0 || svgBounds.isEmpty) return

            val values = FloatArray(9)
            viewMatrix.getValues(values)
            val currentScale = values[Matrix.MSCALE_X]

            val centerX = width / 2f
            val centerY = height / 2f

            val scaleFactor = targetScale / currentScale
            viewMatrix.postScale(scaleFactor, scaleFactor, centerX, centerY)

            constrainViewMatrix()
            notifyViewportChange()
            invalidate()
        }

        fun getCurrentViewportState(): ViewportState? {
            if (width <= 0 || height <= 0 || svgBounds.isEmpty) return null

            val values = FloatArray(9)
            viewMatrix.getValues(values)

            return ViewportState(
                scale = values[Matrix.MSCALE_X],
                translateX = values[Matrix.MTRANS_X],
                translateY = values[Matrix.MTRANS_Y],
                svgWidth = svgBounds.width(),
                svgHeight = svgBounds.height(),
                viewWidth = width,
                viewHeight = height,
                originalScale = minScaleFactor / 0.9f,
            )
        }

        fun initSegmentFile(
            svgWidth: Float,
            svgHeight: Float,
            segments: List<SegmentUIState>,
        ) {
            // Small trick to not apply the re-init the matrix since it reset the view
            val shouldInitMatrix = segmentUIStates.isEmpty()
            segmentUIStates.clear()
            segmentUIStates.addAll(segments)
            svgBounds.set(0f, 0f, svgWidth, svgHeight)
            isInitialSetupDone = false
            if (width > 0 && height > 0 && shouldInitMatrix) {
                setupInitialTransform()
            }
            invalidateCache()
            invalidate()
        }

        fun invalidateCache() {
            isCacheValid = false
            clearGridOverlayCache()
        }

        private fun clearGridOverlayCache() {
            gridOverlayCache.values.forEach { it.recycle() }
            gridOverlayCache.clear()
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
            strokeSvgPicture?.let { picture ->
                cacheCanvas.withMatrix(strokeMatrix) {
                    drawPicture(picture)
                }
            }

            isCacheValid = true
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
                        segmentUIStates.forEach { uiState ->
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
                    }
                }
            } else {
                canvas.withMatrix(viewMatrix) {
                    segmentUIStates.forEach { uiState ->
                        fillPaint.color =
                            when {
                                highlightedSegmentId == uiState.id -> Color.RED
                                !uiState.isColored -> Color.WHITE
                                else -> uiState.targetColor
                            }
                        drawPath(uiState.segment.path, fillPaint)
//                        if (selectedLayerNumber > 0 &&
//                            uiState.layerNumber == selectedLayerNumber &&
//                            !uiState.isColored
//                        ) {
//                            drawGridOverlay(this, uiState.segment)
//                        }
                    }
//                    strokeSvgPicture?.let { picture ->
//                        canvas.withMatrix(strokeMatrix) {
//                            drawPicture(picture)
//                        }
//                    }
//                    segmentUIStates.forEach { uiState ->
//                        if (shouldShowLayerNumber(uiState)) {
//                            drawLayerNumber(this, uiState)
//                        }
//                    }
                }
            }
        }

        private fun drawGridOverlay(
            canvas: Canvas,
            segment: Segments,
        ) {
            val segmentId = segment.hashCode()
            val cachedBitmap =
                gridOverlayCache.getOrPut(segmentId) {
                    createGridOverlayBitmap(segment)
                }
            val bounds = segment.region.bounds
            canvas.drawBitmap(
                cachedBitmap,
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                gridBitmapPaint,
            )
        }

        private fun createGridOverlayBitmap(segment: Segments): Bitmap {
            val bounds = segment.region.bounds
            val width = bounds.width().coerceAtLeast(1)
            val height = bounds.height().coerceAtLeast(1)

            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)

            canvas.translate(-bounds.left.toFloat(), -bounds.top.toFloat())

            val texturePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    val shader =
                        android.graphics.BitmapShader(
                            textureBitmap,
                            android.graphics.Shader.TileMode.REPEAT,
                            android.graphics.Shader.TileMode.REPEAT,
                        )
                    val matrix = Matrix()
                    matrix.setScale(0.1f, 0.1f)
                    shader.setLocalMatrix(matrix)
                    this.shader = shader
                }

            canvas.withClip(segment.path) {
                drawPaint(texturePaint)
            }

            return bitmap
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

            return textWidth <= scaledWidth * 0.8f && textHeight <= scaledHeight * 0.8f && !uiState.isColored
        }

        private fun drawLayerNumber(
            canvas: Canvas,
            uiState: SegmentUIState,
        ) {
            val segment = uiState.segment
            val bounds = segment.region.bounds
            val textSize = bounds.width().coerceAtMost(bounds.height()) / 4f
            textPaint.textSize = textSize
            textStrokePaint.textSize = textSize

            val text = uiState.layerNumber.toString()
            val x = bounds.centerX().toFloat()
            val y = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

            canvas.withClip(segment.path) {
                drawText(text, x, y, textStrokePaint)
                drawText(text, x, y, textPaint)
            }
        }

        fun showHint() {
            val firstUncoloredSegment = segmentUIStates.firstOrNull { !it.isColored } ?: return

            selectedColor = firstUncoloredSegment.targetColor
            selectedOriginalColor = firstUncoloredSegment.segment.originalColor
            updateSelectedLayerNumber()

            val bounds = firstUncoloredSegment.segment.region.bounds
            if (bounds.isEmpty || width <= 0 || height <= 0) return

            val segmentCenterX = bounds.exactCenterX()
            val segmentCenterY = bounds.exactCenterY()
            val segmentWidth = bounds.width().toFloat()
            val segmentHeight = bounds.height().toFloat()

            val scaleX = (width * 0.6f) / segmentWidth
            val scaleY = (height * 0.6f) / segmentHeight
            val targetScale = minOf(scaleX, scaleY).coerceIn(minScaleFactor, 30f)

            val viewCenterX = width / 2f
            val viewCenterY = height / 2f

            viewMatrix.reset()
            viewMatrix.postScale(targetScale, targetScale)

            val scaledSegmentX = segmentCenterX * targetScale
            val scaledSegmentY = segmentCenterY * targetScale

            val translateX = viewCenterX - scaledSegmentX
            val translateY = viewCenterY - scaledSegmentY

            viewMatrix.postTranslate(translateX, translateY)

            constrainViewMatrix()
            notifyViewportChange()
            invalidate()
        }

        fun showNextHint() {
            val uncoloredSegments = segmentUIStates.filter { !it.isColored }
            if (uncoloredSegments.isEmpty()) return

            currentHintIndex = (currentHintIndex + 1) % uncoloredSegments.size
            val nextSegment = uncoloredSegments[currentHintIndex]

            highlightedSegmentId = nextSegment.id
            selectedColor = nextSegment.targetColor
            selectedOriginalColor = nextSegment.segment.originalColor

            val bounds = RectF()
            nextSegment.segment.path.computeBounds(bounds, true)
            if (width <= 0 || height <= 0) return

            val segmentCenterX = bounds.centerX()
            val segmentCenterY = bounds.centerY()
            val segmentWidth = bounds.width().toFloat()
            val segmentHeight = bounds.height().toFloat()

            val scaleX = (width * 0.6f) / segmentWidth
            val scaleY = (height * 0.6f) / segmentHeight
            val targetScale = minOf(scaleX, scaleY).coerceIn(minScaleFactor, 40f)

            val viewCenterX = width / 2f
            val viewCenterY = height / 2f

            viewMatrix.reset()
            viewMatrix.postScale(targetScale, targetScale)

            val scaledSegmentX = segmentCenterX * targetScale
            val scaledSegmentY = segmentCenterY * targetScale

            val translateX = viewCenterX - scaledSegmentX
            val translateY = viewCenterY - scaledSegmentY

            viewMatrix.postTranslate(translateX, translateY)

            constrainViewMatrix()
            notifyViewportChange()
            invalidate()
        }

        fun showPrevHint() {
            val uncoloredSegments = segmentUIStates.filter { !it.isColored }
            if (uncoloredSegments.isEmpty()) return

            currentHintIndex = ((currentHintIndex - 1) + uncoloredSegments.size) % uncoloredSegments.size
            val nextSegment = uncoloredSegments[currentHintIndex]

            highlightedSegmentId = nextSegment.id
            selectedColor = nextSegment.targetColor
            selectedOriginalColor = nextSegment.segment.originalColor

            val bounds = RectF()
            nextSegment.segment.path.computeBounds(bounds, true)
            if (width <= 0 || height <= 0) return

            val segmentCenterX = bounds.centerX()
            val segmentCenterY = bounds.centerY()
            val segmentWidth = bounds.width().toFloat()
            val segmentHeight = bounds.height().toFloat()

            val scaleX = (width * 0.6f) / segmentWidth
            val scaleY = (height * 0.6f) / segmentHeight
            val targetScale = minOf(scaleX, scaleY).coerceIn(minScaleFactor, 40f)

            val viewCenterX = width / 2f
            val viewCenterY = height / 2f

            viewMatrix.reset()
            viewMatrix.postScale(targetScale, targetScale)

            val scaledSegmentX = segmentCenterX * targetScale
            val scaledSegmentY = segmentCenterY * targetScale

            val translateX = viewCenterX - scaledSegmentX
            val translateY = viewCenterY - scaledSegmentY

            viewMatrix.postTranslate(translateX, translateY)

            constrainViewMatrix()
            notifyViewportChange()
            invalidate()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            cachedCanvasBitmap?.recycle()
            cachedCanvasBitmap = null
            cachedCanvasBitmapCanvas = null
            clearGridOverlayCache()
            textureBitmap.recycle()
        }
    }
