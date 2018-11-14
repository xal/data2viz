package io.data2viz.viz

import android.content.*
import android.graphics.*
import io.data2viz.timer.*

typealias ALinearGradient = android.graphics.LinearGradient
typealias ARadialGradient = android.graphics.RadialGradient

val paint = Paint().apply {
    isAntiAlias = true
}

fun Paint.getNumberHeight(): Int {
    val rect = android.graphics.Rect()
    getTextBounds("a", 0, 1, rect)
    return rect.height()
}

class AndroidCanvasRenderer(
    override val viz: Viz,
    val context: Context,
    var canvas: Canvas = Canvas()
) : VizRenderer {

    var scale = 1F

    private val animationTimers = mutableListOf<Timer>()


    init {
        viz.renderer = this
    }

    override fun render() {
        viz.layers.forEach { layer ->
            if (layer.visible)
                layer.render(this)
        }
    }

    override fun startAnimations() {
        if (viz.animations.isNotEmpty()) {
            viz.animations.forEach { anim ->
                animationTimers += timer { time ->
                    anim(time)
                }
            }
            animationTimers += timer {
                render()
            }
        }
    }

    override fun stopAnimations() {
        animationTimers.forEach { it.stop() }
    }


    val Double.dp: Float
        get() = (this * scale).toFloat()

}
