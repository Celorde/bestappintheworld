package com.example.bestappintheworld.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CannonBallGame(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var touchX = 0f
    private var touchY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var launched = false

    private var ballX = 0f
    private var ballY = 0f
    private var velocityX = 0f
    private var velocityY = 0f

    private val gravity = 980f // pixels/s^2
    private var lastTime = System.nanoTime()
    private val radius = 30f

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
    }

    private val rulerPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        textSize = 30f
        isAntiAlias = true
    }

    private var distance = 0f
    private var groundY = 0f
    private var startX = 0f
    private var startY = 0f

    var onDistanceMeasured: ((Float) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        groundY = h - 100f
        resetBall()
    }

    private fun resetBall() {
        onDistanceMeasured?.invoke(distance)
        startX = radius + 20f
        startY = groundY - radius
        ballX = startX
        ballY = startY
        launched = false
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (launched) {
                    resetBall()
                    return true
                }

                touchX = event.x
                touchY = event.y
                startX = touchX
                startY = touchY
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x
                currentY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (!launched) {
                    val dx = startX - event.x
                    val dy = startY - event.y
                    velocityX = dx * 3 * event.pointerCount
                    velocityY = dy * 3 * event.pointerCount
                    launched = true
                    lastTime = System.nanoTime()
                    post(updateRunnable)
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val interval = 100f
        val totalWidth = width.toFloat()
        val rulerY = groundY + 30f
        val tickHeight = 20f

        var x = 0f
        while (x <= totalWidth) {
            canvas.drawLine(x, groundY, x, groundY + tickHeight, rulerPaint)
            canvas.drawText("${x.toInt()}", x + 5f, rulerY + 30f, rulerPaint)
            x += interval
        }

        canvas.drawColor(Color.WHITE)
        canvas.drawCircle(ballX, ballY, radius, paint)

        if (!launched) {
            canvas.drawLine(currentX, currentY, startX, startY, linePaint)
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (launched) {
                val currentTime = System.nanoTime()
                val deltaTime = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                ballX += velocityX * deltaTime
                ballY += velocityY * deltaTime
                velocityY += gravity * deltaTime

                if (ballY + radius >= groundY) {
                    ballY = groundY - radius
                    launched = false
                    distance = ballX - 50f
                    println("Distância percorrida: ${"%.2f".format(distance)} pixels")
                    resetBall()
                    return
                }

                invalidate()
                postDelayed(this, 16)
            }
        }
    }
}
