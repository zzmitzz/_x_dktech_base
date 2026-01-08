package com.emulator.retro.console.game.customviews

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import com.emulator.retro.console.game.R

class SelectableTextViewNovoSyx(
    mContext: Context, attrs: AttributeSet? = null
) : AppCompatTextView(mContext, attrs) {

    private val durationAnim by lazy { resources.getInteger(android.R.integer.config_shortAnimTime).toLong() }

    @ColorInt
    private var mColorSelected: Int = Color.WHITE
    private var mColorUnselected: Int = Color.BLACK
    private var mAnimateColor: ValueAnimator? = null

    init {
        init(mContext, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.SelectableTextView, 0, 0
        )
        try {
            if (typedArray.hasValue(R.styleable.SelectableTextView_color_selected)) {
                mColorSelected = typedArray.getColor(R.styleable.SelectableTextView_color_selected, Color.WHITE)
            }
            if (typedArray.hasValue(R.styleable.SelectableTextView_color_unselected)) {
                mColorUnselected = typedArray.getColor(R.styleable.SelectableTextView_color_unselected, Color.BLACK)
            }
        } finally {
            typedArray.recycle()
        }

        setTextColor(mColorUnselected)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (mAnimateColor?.isRunning == true) {
            mAnimateColor?.cancel()
            mAnimateColor = null
        }

        mAnimateColor = if (selected) {
            ValueAnimator.ofObject(ArgbEvaluator(), mColorUnselected, mColorSelected)
        } else {
            ValueAnimator.ofObject(ArgbEvaluator(), mColorSelected, mColorUnselected)
        }
        mAnimateColor?.setDuration(durationAnim)
        mAnimateColor?.addUpdateListener {
            setTextColor(it.animatedValue as Int)
        }
        mAnimateColor?.start()
    }

}