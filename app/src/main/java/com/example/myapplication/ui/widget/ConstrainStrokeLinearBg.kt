package com.example.myapplication.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.mybase.extensions.loadAttrs


class ConstrainStrokeLinearBg(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    private var cornerRadius = 0f
    private var borderWidth = 0f
    private var startColor = 0
    private var centerColor = 0
    private var endColor = 0
    private var bgColor = 0

    private val path = Path()
    private val borderPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    init {
        val resource = context.resources
        context.loadAttrs(attributeSet, R.styleable.ViewStrokeBg) {
            borderWidth = getDimension(R.styleable.ViewStrokeBg_borderWidth, 10f)
            cornerRadius = getDimension(R.styleable.ViewStrokeBg_cornerRadius, 20f)
            startColor = getColor(R.styleable.ViewStrokeBg_startColor, ResourcesCompat.getColor(resource, R.color.start_bg_ai_box, null))
            centerColor = getColor(R.styleable.ViewStrokeBg_centerColor,  ResourcesCompat.getColor(resource, R.color.center_bg_ai_box, null))
            endColor = getColor(R.styleable.ViewStrokeBg_endColor,  ResourcesCompat.getColor(resource, R.color.end_bg_ai_box, null))
            bgColor = getColor(R.styleable.ViewStrokeBg_ai_background, Color.WHITE)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Create and set your gradient here so that the gradient size is always correct
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 30f
        borderPaint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            intArrayOf(startColor, centerColor, endColor),
            null,
            Shader.TileMode.CLAMP
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
//        path.rewind()
        path.addRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            100f,
            100f,
            Path.Direction.CCW
        )
        canvas.drawPath(path, borderPaint);
//        path.rewind()
//        // Define the shape of the additional area you want to fill with the new color
//        // For example, to fill a horizontal stripe at the bottom:
//        path.addRoundRect(
//            borderWidth,
//            borderWidth,
//            width.toFloat() - borderWidth,
//            height.toFloat() - borderWidth,
//            cornerRadius - borderWidth / 2,
//            cornerRadius - borderWidth / 2,
//            Path.Direction.CCW
//        )
//        borderPaint.reset()
//        borderPaint.apply {
//            style = Paint.Style.FILL
//            color = bgColor
//        }

//        canvas.drawPath(path, borderPaint)
    }

}