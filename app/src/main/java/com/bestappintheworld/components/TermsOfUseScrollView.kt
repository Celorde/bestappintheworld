package com.bestappintheworld.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.OverScroller
import android.widget.ScrollView
import kotlin.math.abs
import kotlin.math.sign


class TermsOfUseScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

    private val upScrollMultiplier = 2.0f     // acelera o scroll pra cima
    private val downScrollMultiplier = 0.5f   // desacelera o scroll pra baixo

    private var lastY = 0f
    private var velocityTracker: VelocityTracker? = null
    private var scroller: OverScroller? = null

    init {
        // Garante que tenhamos acesso ao mesmo scroller interno do ScrollView
        try {
            val field = ScrollView::class.java.getDeclaredField("mScroller")
            field.isAccessible = true
            scroller = field.get(this) as? OverScroller
        } catch (_: Exception) { }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.y
                if (!scroller?.isFinished!!) {
                    scroller?.abortAnimation()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - lastY
                lastY = ev.y

                // 🔹 Aplicamos o controle de velocidade manual apenas durante o arrasto
                val adjustedDelta = when {
                    deltaY < 0 -> deltaY * downScrollMultiplier
                    deltaY > 0 -> deltaY * upScrollMultiplier
                    else -> deltaY
                }

                scrollBy(0, (-adjustedDelta).toInt())
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.computeCurrentVelocity(1000)

                val velocityY = velocityTracker?.yVelocity ?: 0f
                if (abs(velocityY) > 2000) { // mínimo necessário pra gerar um fling perceptível
                    performCustomFling(-velocityY.toInt())
                }

                velocityTracker?.recycle()
                velocityTracker = null
            }
        }

        return true
    }

    private fun performCustomFling(velocityY: Int) {
        // 🔹 Aplica os multiplicadores conforme direção
        val direction = velocityY.sign
        val adjustedVelocity = when {
            direction > 0 -> (velocityY * 0.15).toInt()
            direction < 0 -> (velocityY * upScrollMultiplier).toInt()
            else -> velocityY
        }

        super.fling(adjustedVelocity)
    }
}
