package io.data2viz.viz

import io.data2viz.timer.Timer
import io.data2viz.timer.timer
import javafx.scene.canvas.Canvas
import kotlin.math.PI


/**
 * JFx Canvas version. See https://docs.oracle.com/javafx/2/canvas/jfxpub-canvas.htm
 */
class JFxVizRenderer(
    override val viz: Viz,
    val canvas: Canvas
) : View {

    internal val gc = canvas.graphicsContext2D

    private val animationTimers = mutableListOf<Timer>()

    init {
        viz.view = this
    }


    override fun render() {
        gc.clearRect(.0, .0, canvas.width, canvas.height)
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

    fun addTransform(transform: Transform) {
        gc.translate(transform.translate?.x ?: .0, transform.translate?.y ?:.0)
        gc.rotate(+ (transform.rotate?.delta ?: .0) * 180 / PI)
    }

    fun removeTransform(transform: Transform) {
        gc.translate(-(transform.translate?.x ?:.0), -(transform.translate?.y ?:.0))
        gc.rotate(- (transform.rotate?.delta ?: .0) * 180 / PI)
    }

}


