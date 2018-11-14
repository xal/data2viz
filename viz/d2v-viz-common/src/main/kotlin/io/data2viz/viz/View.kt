package io.data2viz.viz


interface View: VizRenderer {

    /**
     * Starts all animations
     */
    fun startAnimations()

    /**
     * Stops all animations.
     */
    fun stopAnimations()

}
