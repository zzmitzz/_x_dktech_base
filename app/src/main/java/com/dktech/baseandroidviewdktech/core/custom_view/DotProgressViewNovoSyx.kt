package com.emulator.retro.console.game.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.emulator.retro.console.game.R
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

class DotProgressViewNovoSyx(mContext: Context, attrs: AttributeSet? = null) : View(mContext, attrs) {

    private val mDensity = mContext.resources.displayMetrics.density

    private var mProgressHeight: Float = 10f * mDensity

    private var mRect = RectF()

    // region Background
    private var mBackgroundColor: Int = Color.TRANSPARENT
    private var mBackgroundStrokeColor: Int = Color.WHITE
    private var mBackgroundStrokeWidth: Float = 2f * mDensity
    // endregion

    // region Dot
    private var mDotTime: Long = 400L
    private val mRandomDotTime: Long
        get() = mDotTime + Random.nextLong(-mDotTime / 2, mDotTime / 2)
    private var mDotColor: Int = Color.CYAN
    private var mDotWidth: Float = 4f * mDensity
    private var mDotSpace: Float = 2f * mDensity
    // endregion

    // region Shadow
    private var mShadowColor: Int = Color.BLACK
    private var mShadowDx = 0.5f * mDensity
    private var mShadowDy = 0.5f * mDensity
    // endregion

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.DotProgressView, 0, 0
            )

            try {
                if (typedArray.hasValue(R.styleable.DotProgressView_mThinness)) {
                    mProgressHeight = typedArray.getDimension(
                        R.styleable.DotProgressView_mThinness, mProgressHeight
                    ).coerceAtLeast(10f * mDensity)
                }

                // region Background
                if (typedArray.hasValue(R.styleable.DotProgressView_mBackgroundColor)) {
                    mBackgroundColor = typedArray.getColor(
                        R.styleable.DotProgressView_mBackgroundColor, mBackgroundColor
                    )
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mBackgroundStrokeColor)) {
                    mBackgroundStrokeColor = typedArray.getColor(
                        R.styleable.DotProgressView_mBackgroundStrokeColor, mBackgroundStrokeColor
                    )
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mBackgroundStrokeWidth)) {
                    mBackgroundStrokeWidth = typedArray.getDimension(
                        R.styleable.DotProgressView_mBackgroundStrokeWidth, mBackgroundStrokeWidth
                    ).coerceIn(2f * mDensity, mProgressHeight / 5f)
                }
                // endregion

                // region Dot
                if (typedArray.hasValue(R.styleable.DotProgressView_mDotTime)) {
                    mDotTime = typedArray.getInteger(
                        R.styleable.DotProgressView_mDotTime, 400
                    ).coerceAtLeast(400).toLong()
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mDotColor)) {
                    mDotColor = typedArray.getColor(
                        R.styleable.DotProgressView_mDotColor, mDotColor
                    )
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mDotWidth)) {
                    mDotWidth = typedArray.getDimension(
                        R.styleable.DotProgressView_mDotWidth, mDotWidth
                    )
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mDotSpace)) {
                    mDotSpace = typedArray.getDimension(
                        R.styleable.DotProgressView_mDotSpace, mDotSpace
                    )
                }
                // endregion

                // region Shadow
                if (typedArray.hasValue(R.styleable.DotProgressView_mShadowColor)) {
                    mShadowColor = typedArray.getColor(
                        R.styleable.DotProgressView_mShadowColor, mShadowColor
                    )
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mShadowDx)) {
                    mShadowDx = typedArray.getDimension(
                        R.styleable.DotProgressView_mShadowDx, mShadowDx
                    )
                }
                if (typedArray.hasValue(R.styleable.DotProgressView_mShadowDy)) {
                    mShadowDy = typedArray.getDimension(
                        R.styleable.DotProgressView_mShadowDy, mShadowDy
                    )
                }
                // endregion
            } finally {
                typedArray.recycle()
            }
        }
    }

    private val mPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }

    private var mProgress: Float = paddingLeft + mBackgroundStrokeWidth
    private var mUpdateProgressRunnable = Runnable {
        mProgress = if (mProgress >= width - 2 * mBackgroundStrokeWidth) {
            paddingLeft + mBackgroundStrokeWidth
        } else {
            mProgress + mDotWidth + mDotSpace
        }

        val mStrokeWidth = mBackgroundStrokeWidth.toInt()
        postInvalidateOnAnimation(
            mStrokeWidth,
            paddingTop + mStrokeWidth,
            width - mStrokeWidth,
            height - paddingBottom - mStrokeWidth
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST)
        val measuredHeight = MeasureSpec.makeMeasureSpec(
            mProgressHeight.roundToInt() + paddingTop + paddingBottom,
            MeasureSpec.EXACTLY
        )
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        removeCallbacks(mUpdateProgressRunnable)
        super.onDraw(canvas)

        val mLeft = mRect.left
        val mTop = mRect.top
        val mRight = mRect.right
        val mBottom = mRect.bottom
        val mBackgroundStrokeWidth = this.mBackgroundStrokeWidth

        drawDotProgress(
            canvas, mLeft, mTop, mRight, mBottom, mBackgroundStrokeWidth,
        )

        canvas.withSave {
            clipOutRect(
                mLeft + mBackgroundStrokeWidth,
                mTop + mBackgroundStrokeWidth,
                mRight - mBackgroundStrokeWidth,
                mBottom - mBackgroundStrokeWidth,
            )
            drawBackground(
                this, mLeft, mTop, mRight, mBottom, mStrokeWidth = mBackgroundStrokeWidth
            )
        }

        postDelayed(mUpdateProgressRunnable, mRandomDotTime)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(mUpdateProgressRunnable)
    }

    private fun drawBackground(
        mCanvas: Canvas,
        mLeft: Float,
        mTop: Float,
        mRight: Float,
        mBottom: Float,
        mColor: Int = mBackgroundColor,
        mStrokeWidth: Float = mBackgroundStrokeWidth,
        mStrokeColor: Int = mBackgroundStrokeColor
    ) {
        // Shadow
        mPaint.color = mShadowColor
        mCanvas.withTranslation(mShadowDx, mShadowDy) {
            mCanvas.drawRect(
                mLeft, mTop + mStrokeWidth, mLeft + mStrokeWidth, mBottom - mStrokeWidth, mPaint
            )
            mCanvas.drawRect(
                mRight - mStrokeWidth, mTop + mStrokeWidth, mRight, mBottom - mStrokeWidth, mPaint
            )
            mCanvas.drawRect(
                mLeft + mStrokeWidth, mTop, mRight - mStrokeWidth, mBottom, mPaint
            )
        }

//        // Stroke of stroke
//        mPaint.color = mShadowColor
//        mCanvas.drawRect(
//            mLeft - mDensity, mTop + mStrokeWidth - mDensity, mLeft + mStrokeWidth + mDensity, mBottom - mStrokeWidth + mDensity, mPaint
//        )
//        mCanvas.drawRect(
//            mLeft + mStrokeWidth - mDensity, mTop - mDensity, mRight - mStrokeWidth + mDensity, mTop + mStrokeWidth+ mDensity, mPaint
//        )
//        mCanvas.drawRect(
//            mRight - mStrokeWidth - mDensity, mTop + mStrokeWidth - mDensity, mRight + mDensity, mBottom - mStrokeWidth+ mDensity, mPaint
//        )
//        mCanvas.drawRect(
//            mLeft + mStrokeWidth - mDensity, mBottom - mStrokeWidth - mDensity, mRight - mStrokeWidth + mDensity, mBottom+ mDensity, mPaint
//        )

        // Stroke
        mPaint.color = mStrokeColor
        mCanvas.drawRect(
            mLeft, mTop + mStrokeWidth, mLeft + mStrokeWidth, mBottom - mStrokeWidth, mPaint
        )
        mCanvas.drawRect(
            mLeft + mStrokeWidth, mTop, mRight - mStrokeWidth, mTop + mStrokeWidth, mPaint
        )
        mCanvas.drawRect(
            mRight - mStrokeWidth, mTop + mStrokeWidth, mRight, mBottom - mStrokeWidth, mPaint
        )
        mCanvas.drawRect(
            mLeft + mStrokeWidth, mBottom - mStrokeWidth, mRight - mStrokeWidth, mBottom, mPaint
        )

        // Solid
        mPaint.color = mColor
        if (mColor == Color.TRANSPARENT) {
            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        mCanvas.drawRect(
            mLeft + mStrokeWidth,
            mTop + mStrokeWidth,
            mRight - mStrokeWidth,
            mBottom - mStrokeWidth,
            mPaint
        )

        if (mColor == Color.TRANSPARENT) {
            mPaint.xfermode = null
        }
    }

    private fun drawDotProgress(
        mCanvas: Canvas,
        mLeft: Float,
        mTop: Float,
        mRight: Float,
        mBottom: Float,
        mBackgroundStrokeWidth: Float,
        mProgress: Float = this.mProgress,
        mDotWidth: Float = this.mDotWidth,
        mColor: Int = mDotColor,
        mSpace: Float = mDotSpace
    ) {
        mPaint.color = mColor

        val mDotTop = mTop + mBackgroundStrokeWidth + 2 * mDensity
        val mDotBottom = mBottom - mBackgroundStrokeWidth - 2 * mDensity
        val mLast = min(mProgress, mRight - mBackgroundStrokeWidth - mSpace)
        var x = mLeft + mBackgroundStrokeWidth
        while (x + mSpace + mDotWidth < mLast) {
            mCanvas.drawRect(x + mSpace, mDotTop, x + mSpace + mDotWidth, mDotBottom, mPaint)
            x += mSpace + mDotWidth
        }

        if (x + mSpace > mLast) return

        mCanvas.drawRect(x + mSpace, mDotTop, mLast, mDotBottom, mPaint)
    }

}