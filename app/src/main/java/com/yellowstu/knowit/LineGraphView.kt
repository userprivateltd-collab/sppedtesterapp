package com.yellowstu.knowit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class LineGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = mutableListOf<Float>()
    private val maxDataPoints = 20

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val fillPath = Path()

    fun addDataPoint(value: Float) {
        dataPoints.add(value)
        if (dataPoints.size > maxDataPoints) {
            dataPoints.removeAt(0)
        }
        invalidate()
    }

    fun clear() {
        dataPoints.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.size < 2) return

        val width = width.toFloat()
        val height = height.toFloat()
        val maxValue = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(10f) * 1.2f

        path.reset()
        fillPath.reset()

        val xStep = width / (maxDataPoints - 1)
        
        dataPoints.forEachIndexed { index, value ->
            val x = index * xStep
            val y = height - (value / maxValue * height)
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (index == dataPoints.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Create gradient for fill
        fillPaint.shader = LinearGradient(0f, 0f, 0f, height,
            intArrayOf(Color.parseColor("#3300E5FF"), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP)

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)
    }
}
