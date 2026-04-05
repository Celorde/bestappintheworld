package com.bestappintheworld.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

data class Circle(val cx: Float, val cy: Float, val r: Float)

class PerfectCircleDraw(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val path = Path()
    private val points = mutableListOf<PointF>()

    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)

        val x = event.x
        val y = event.y

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                path.reset()
                points.clear()

                path.moveTo(x, y)
                points.add(PointF(x, y))

                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                points.add(PointF(x, y))
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                points.add(PointF(x, y))
                onCircleFinished()
                invalidate()
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    var onCircleFinished: () -> Unit = {}

    fun estimateCircle(points: List<PointF>): Circle {
        val cx = points.map { it.x }.average().toFloat()
        val cy = points.map { it.y }.average().toFloat()

        val radiusList = points.map { p ->
            hypot((p.x - cx), (p.y - cy))
        }

        val r = radiusList.average().toFloat()

        return Circle(cx, cy, r)
    }

    fun calculatePerfection(): Float {
        val circle = estimateCircle(points)

        val distances = points.map { p ->
            hypot((p.x - circle.cx), (p.y - circle.cy))
        }

        val r = circle.r

        val maxR = distances.max()
        val minR = distances.min()

        val radiusDiff = maxR - minR
        val diameter = r * 2

        val precision = 1f - (radiusDiff / diameter)

        return (precision * 100f).coerceIn(0f, 100f)
    }
}
