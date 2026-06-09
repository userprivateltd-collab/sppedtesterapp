package com.yellowstu.knowit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SpeedGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private val strokeWidth = 40f
    
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1D2E")
        style = Paint.Style.STROKE
        this.strokeWidth = this@SpeedGaugeView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = this@SpeedGaugeView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = this@SpeedGaugeView.strokeWidth + 20f
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
    }

    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val size = if (width < height) width else height
        val pad = strokeWidth / 2 + 40

        rectF.set(pad, pad, size - pad, size - pad)

        // Draw background arc
        canvas.drawArc(rectF, 150f, 240f, false, backgroundPaint)

        // Setup gradient
        val gradient = SweepGradient(size / 2, size / 2, 
            intArrayOf(Color.parseColor("#00E5FF"), Color.parseColor("#D500F9")),
            floatArrayOf(0f, 1f)
        )
        val matrix = Matrix()
        matrix.setRotate(150f, size / 2, size / 2)
        gradient.setLocalMatrix(matrix)
        
        progressPaint.shader = gradient
        glowPaint.shader = gradient

        // Draw glow and progress arc
        val sweepAngle = (progress / 100f) * 240f
        canvas.drawArc(rectF, 150f, sweepAngle, false, glowPaint)
        canvas.drawArc(rectF, 150f, sweepAngle, false, progressPaint)
    }

    fun setSpeed(value: Float) {
        this.progress = value.coerceIn(0f, 100f)
        invalidate()
    }
}
