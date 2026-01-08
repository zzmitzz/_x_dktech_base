package com.dktech.baseandroidviewdktech.core.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    var strokeWidth = 8f
    var strokeColor = Color.BLACK
    var fillShader: Shader? = null   // your gradient or shader

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val textString = text?.toString() ?: return
        val x = paddingLeft.toFloat()
        val y = baseline.toFloat()

        // ---- Stroke Paint ----
        strokePaint.apply {
            textSize = paint.textSize
            typeface = paint.typeface
            style = Paint.Style.STROKE
            color = strokeColor
            strokeWidth = this@StrokeTextView.strokeWidth
            shader = null        // NO SHADER on stroke
        }

        // draw stroke
        canvas.drawText(textString, x, y, strokePaint)

        // ---- Fill Paint ----
        fillPaint.apply {
            textSize = paint.textSize
            typeface = paint.typeface
            style = Paint.Style.FILL
            color = currentTextColor
            shader = fillShader   // APPLY SHADER ONLY HERE
        }

        // draw fill
        canvas.drawText(textString, x, y, fillPaint)
    }
}