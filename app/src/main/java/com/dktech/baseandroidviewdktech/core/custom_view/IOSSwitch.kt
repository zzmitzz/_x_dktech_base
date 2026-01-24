package com.dktech.baseandroidviewdktech.core.custom_view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.dktech.baseandroidviewdktech.R

class IOSSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isChecked = false
    private var thumbPosition = 0f
    private var animator: ValueAnimator? = null
    
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val trackRect = RectF()
    private val thumbRect = RectF()
    
    private var trackColorOn = 0xFF0071C7.toInt()
    private var trackColorOff = 0xFFEFEFEF.toInt()
    private var thumbColorOn = 0xFFFFFFFF.toInt()
    private var thumbColorOff = 0xFFD9D9D9.toInt()
    
    private val trackWidth = 60f.dpToPx()
    private val trackHeight = 32f.dpToPx()
    private val thumbSize = 26f.dpToPx()
    private val thumbPadding = 3f.dpToPx()
    
    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null
    
    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.IOSSwitch,
            0, 0
        ).apply {
            try {
                isChecked = getBoolean(R.styleable.IOSSwitch_android_checked, false)
                trackColorOn = getColor(R.styleable.IOSSwitch_trackColorOn, trackColorOn)
                trackColorOff = getColor(R.styleable.IOSSwitch_trackColorOff, trackColorOff)
                thumbColorOn = getColor(R.styleable.IOSSwitch_thumbColorOn, thumbColorOn)
                thumbColorOff = getColor(R.styleable.IOSSwitch_thumbColorOff, thumbColorOff)
            } finally {
                recycle()
            }
        }
        
        thumbPosition = if (isChecked) 1f else 0f
        
        thumbShadowPaint.apply {
            color = 0x40000000
            setShadowLayer(4f.dpToPx(), 0f, 2f.dpToPx(), 0x40000000)
        }
        
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = trackWidth.toInt() + paddingLeft + paddingRight
        val desiredHeight = trackHeight.toInt() + paddingTop + paddingBottom
        
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        
        setMeasuredDimension(width, height)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val centerX = w / 2f
        val centerY = h / 2f
        
        trackRect.set(
            centerX - trackWidth / 2f,
            centerY - trackHeight / 2f,
            centerX + trackWidth / 2f,
            centerY + trackHeight / 2f
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val currentTrackColor = interpolateColor(trackColorOff, trackColorOn, thumbPosition)
        val currentThumbColor = interpolateColor(thumbColorOff, thumbColorOn, thumbPosition)
        
        trackPaint.color = currentTrackColor
        canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, trackPaint)
        
        val thumbCenterX = trackRect.left + thumbPadding + thumbSize / 2f +
                (trackRect.width() - thumbSize - thumbPadding * 2) * thumbPosition
        val thumbCenterY = trackRect.centerY()
        
        thumbRect.set(
            thumbCenterX - thumbSize / 2f,
            thumbCenterY - thumbSize / 2f,
            thumbCenterX + thumbSize / 2f,
            thumbCenterY + thumbSize / 2f
        )
        
        thumbShadowPaint.color = 0x40000000
        canvas.drawCircle(thumbCenterX, thumbCenterY + 1f.dpToPx(), thumbSize / 2f, thumbShadowPaint)
        
        thumbPaint.color = currentThumbColor
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbSize / 2f, thumbPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (event.x >= trackRect.left && event.x <= trackRect.right &&
                    event.y >= trackRect.top && event.y <= trackRect.bottom) {
                    toggle()
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
    
    fun setChecked(checked: Boolean, animate: Boolean = true) {
        if (isChecked == checked) return
        
        isChecked = checked
        
        animator?.cancel()
        
        if (animate) {
            val targetPosition = if (checked) 1f else 0f
            animator = ValueAnimator.ofFloat(thumbPosition, targetPosition).apply {
                duration = 250
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    thumbPosition = animation.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            thumbPosition = if (checked) 1f else 0f
            invalidate()
        }
    }
    
    fun toggle() {
        setChecked(!isChecked)
        onCheckedChangeListener?.invoke(isChecked)
    }
    
    fun isChecked(): Boolean = isChecked
    
    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }
    
    private fun Float.dpToPx(): Float {
        return this * context.resources.displayMetrics.density
    }
    
    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = (startColor shr 24) and 0xff
        val startR = (startColor shr 16) and 0xff
        val startG = (startColor shr 8) and 0xff
        val startB = startColor and 0xff
        
        val endA = (endColor shr 24) and 0xff
        val endR = (endColor shr 16) and 0xff
        val endG = (endColor shr 8) and 0xff
        val endB = endColor and 0xff
        
        return ((startA + (fraction * (endA - startA)).toInt()) shl 24) or
                ((startR + (fraction * (endR - startR)).toInt()) shl 16) or
                ((startG + (fraction * (endG - startG)).toInt()) shl 8) or
                (startB + (fraction * (endB - startB)).toInt())
    }
}
