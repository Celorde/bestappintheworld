package com.example.bestuiintheworld.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class ColorPickerTray(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pointerX = 0f
    private var pointerY = 0f
    private var pointerRadius = 20f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (min(width, height) / 2f) - pointerRadius

        val sweepShader = SweepGradient(centerX, centerY, createColorWheel(), null)
        paint.shader = sweepShader
        canvas.drawCircle(centerX, centerY, radius, paint)

        val px = centerX + pointerX * radius
        val py = centerY + pointerY * radius

        canvas.drawCircle(px, py, pointerRadius, Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        })
    }

    private fun createColorWheel(): IntArray {
        val colors = IntArray(361)
        for (i in 0..360) {
            colors[i] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }
        return colors
    }

    fun setPointerPosition(nx: Float, ny: Float) {
        //TODO Inverter pra confirmação de cor
        val distance = sqrt(nx * nx + ny * ny)
        if (distance <= 1f) {
            pointerX = nx
            pointerY = ny
            invalidate()
        } else {
            val scale = 1f / distance
            pointerX = nx * scale
            pointerY = ny * scale
            invalidate()
        }
    }

    fun getSelectedColor(): Int {
        val angle = (atan2(pointerY, pointerX) * (180 / Math.PI)).toFloat()
        val hue = (angle + 360) % 360
        val saturation = sqrt(pointerX * pointerX + pointerY * pointerY).coerceIn(0f, 1f)
        return Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
    }
}
