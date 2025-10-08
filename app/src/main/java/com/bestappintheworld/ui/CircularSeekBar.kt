package com.example.bestappintheworld.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var lastProgress = 0

    var dayOfYear = 0
        private set

    var initialYear = 0

    var onProgressChanged: ((Int, Int, Boolean) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 20f
        style = Paint.Style.STROKE
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height)
        val radius = size / 2f - paint.strokeWidth
        val centerX = width / 2f
        val centerY = height / 2f

        canvas.drawCircle(centerX, centerY, radius, paint)

        val angle = 360f * dayOfYear / 365f - 90f
        val rad = Math.toRadians(angle.toDouble())

        val x = (centerX + radius * cos(rad)).toFloat()
        val y = (centerY + radius * sin(rad)).toFloat()

        canvas.drawCircle(x, y, 20f, pointerPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN &&
            event.action != MotionEvent.ACTION_MOVE &&
            event.action != MotionEvent.ACTION_UP) return true

        val x = event.x - width / 2f
        val y = event.y - height / 2f

        val angle = (atan2(y, x) * 180 / Math.PI + 450) % 360
        val newProgress = ((angle / 360f) * 365).toInt().coerceIn(0, 364)

        val crossedForward = lastProgress > 300 && newProgress < 60
        val crossedBackward = lastProgress < 60 && newProgress > 300

        if (crossedForward) {
            initialYear += 1
        } else if (crossedBackward) {
            initialYear -= 1
        }

        lastProgress = newProgress
        dayOfYear = newProgress
        onProgressChanged?.invoke(initialYear, dayOfYear, event.action == MotionEvent.ACTION_UP)

        invalidate()
        return true
    }
}
