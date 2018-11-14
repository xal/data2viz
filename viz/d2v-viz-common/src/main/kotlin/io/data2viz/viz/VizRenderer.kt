package io.data2viz.viz


/**
 * Common interface for all platform renderers.
 */
interface VizRenderer {

    val viz:Viz

    /**
     * Perform the rendering in the specific platform.
     */
    fun render()

    /**
     * Starts all animations
     */
    fun startAnimations()

    /**
     * Stops all animations.
     */
    fun stopAnimations()
}