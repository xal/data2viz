package io.data2viz.geo.projection

import io.data2viz.geo.geometry.asin
import io.data2viz.geo.geometry.clip.anglePreClip
import io.data2viz.geo.projection.common.ProjectorProjection
import io.data2viz.geo.projection.common.projection
import io.data2viz.math.deg
import kotlin.math.sqrt

fun azimuthalEqualAreaProjection() = azimuthalEqualAreaProjection {}

fun azimuthalEqualAreaProjection(init: ProjectorProjection.() -> Unit) =
    projection(AzimuthalEqualArea()) {
        scale = 124.75
        anglePreClip = (180 - 1e-3).deg
        init()
    }

private val scale: (Double) -> Double = { cxcy -> sqrt(2 / (1 + cxcy)) }
private val angle: (Double) -> Double = { z -> 2 * (z / 2).asin }

/**
 * The azimuthal equal-area projection.
 */
class AzimuthalEqualArea: AzimuthalProjector(scale, angle)