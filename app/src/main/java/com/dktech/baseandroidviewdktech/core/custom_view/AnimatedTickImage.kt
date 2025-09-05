package com.dktech.baseandroidviewdktech.core.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.dktech.baseandroidviewdktech.R

class AnimatedTickImage(
    val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(mContext, attrs, defStyleAttr) {


    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = mContext.getColor(R.color.primary)
    }

    override fun isSelected(): Boolean {
        invalidate()
        return super.isSelected()

    }

    private var rectTick: RectF? = null


    private val tickImage by lazy {
        val tickSize = width / 4f // 25% of the parent size
        val left = width - tickSize - 8f
        val top = 8f
        val right = left + tickSize
        val bottom = top + tickSize
        rectTick = RectF(left, top, right, bottom)
        ContextCompat.getDrawable(mContext, R.drawable.tick)?.toBitmap()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width / 2f - circlePaint.strokeWidth / 2
        val centerX = width / 2f
        val centerY = height / 2f
        if(isSelected){
            canvas.drawCircle(centerX, centerY, radius, circlePaint)
            tickImage?.let {
                if(rectTick != null){
                    canvas.drawBitmap(it, null, rectTick!!, null)
                }
                else{
                    val tickSize = width / 4f // 25% of the parent size
                    val left = width - tickSize - 8f
                    val top = 8f
                    val right = left + tickSize
                    val bottom = top + tickSize
                    rectTick = RectF(left, top, right, bottom)
                    canvas.drawBitmap(it, null, rectTick!!, null)
                }
            }
        }
    }
}