package com.example.ads.activity.widgets

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.example.ads.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CommonCircleLoading : View {

    companion object {
        private const val ARC_WIDTH = 8
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private var mStrokeWidth = -1f
    private var mCurrentAlpha: Float = 255f
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mLoadingRadius = 0f
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mRadiusValue = arrayListOf<Pair<Double, Double>>()
    private val mAnimator = ObjectAnimator.ofFloat(0f, 255f)


    private fun initView(context: Context) {
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.FILL
        mPaint.color = ContextCompat.getColor(context, R.color.color_ads)
        mRadiusValue.clear()
        var i = 0
        while (i < 360) {
            val angle =
                Math.toRadians(i.toDouble()).toFloat() // Need to convert to radians first
            val sinValue = sin(angle.toDouble())
            val cosValue = cos(angle.toDouble())
            mRadiusValue.add(Pair(sinValue, cosValue))
            i += 45
        }
        animateLoading(500)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
        if (mStrokeWidth < 0)
            mStrokeWidth = (size / ARC_WIDTH).toFloat()
        mLoadingRadius = (size.coerceAtMost(size) / 2).toFloat() - mStrokeWidth / 2
        mPaint.strokeWidth = mStrokeWidth
        mCenterX = size / 2f
        mCenterY = size / 2f

    }

    private fun drawCircle(canvas: Canvas) {
        var alphaIndex = 8
        val difAlpha = 255 - mCurrentAlpha
        mRadiusValue.forEach {
            val startX = (mCenterX + mLoadingRadius * it.first)
            val startY = (mCenterY - mLoadingRadius * it.second)
            val stopX = (mCenterX + (mLoadingRadius - mStrokeWidth) * it.first)
            val stopY = (mCenterY - (mLoadingRadius - mStrokeWidth) * it.second)
            val alpha = 255 - 255 * alphaIndex * 0.125 + difAlpha // 0 25 50
            val newAlpha = if (alpha > 255) (alpha - 255).toInt() else alpha.toInt()
            mPaint.alpha = newAlpha
            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                mPaint
            )
            alphaIndex--
        }

    }

    fun animateLoading(duration: Long) {
        mAnimator.duration = duration
        mAnimator.addUpdateListener {
            val value = (it.animatedValue as Float)
            mCurrentAlpha = value
            if (context != null)
                invalidate()
            else stopAnimation()
        }
        mAnimator.interpolator = LinearInterpolator()
        mAnimator.repeatCount = ValueAnimator.INFINITE
        mAnimator.start()
    }

    fun stopAnimation() {
        try {
            mAnimator.cancel()
        } catch (e: Exception) {
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
        mRadiusValue.clear()
    }
}