package io.data2viz.geo.projection

import io.data2viz.geo.ConditionalProjector
import io.data2viz.geo.ProjectableInvertable
import io.data2viz.math.EPSILON
import io.data2viz.math.deg
import kotlin.math.*

fun conicEqualAreaProjection() = conicEqualAreaProjection {}

fun conicEqualAreaProjection(init: ConicProjection.() -> Unit) = conicProjection(ConicEqualAreaConditionalProjector()) {
    scale = 155.424
    center = arrayOf(0.0.deg, 33.6442.deg)
    init()
}

class ConicEqualAreaConditionalProjector(
    val conicEqualAreaProjector: ConicEqualAreaProjector = ConicEqualAreaProjector(),
    val cylindricalEqualAreaProjector: CylindricalEqualAreaProjector = CylindricalEqualAreaProjector()
) : ConicProjectable by conicEqualAreaProjector, ConditionalProjector() {

    override val baseProjector: ProjectableInvertable
        get() = cylindricalEqualAreaProjector
    override val nestedProjector: ProjectableInvertable
        get() = conicEqualAreaProjector
    override val isNeedUseBaseProjector: Boolean
        get() = conicEqualAreaProjector.isPossibleToUseProjector

    override var phi0: Double
        get() = conicEqualAreaProjector.phi0
        set(value) {
            cylindricalEqualAreaProjector.phi0 = value
            conicEqualAreaProjector.phi0 = value
        }
}

class ConicEqualAreaProjector : ConicProjectable, ProjectableInvertable {

    override var phi0: Double = 0.0
        set(value) {
            field = value
            recalculate()
        }
    override var phi1: Double = io.data2viz.math.PI / 3.0
        set(value) {
            field = value
            recalculate()
        }

    private var sy0 = sin(phi0)
    private var n = (sy0 + sin(phi1)) / 2;
    private var c = 1 + sy0 * (2 * n - sy0)
    private var r0 = sqrt(c) / n;
    public var isPossibleToUseProjector = abs(n) < EPSILON

    private fun recalculate() {

        sy0 = sin(phi0)
        n = (sy0 + sin(phi1)) / 2;
        c = 1 + sy0 * (2 * n - sy0)
        r0 = sqrt(c) / n;
        isPossibleToUseProjector = abs(n) < EPSILON
    }


    override fun invert(x: Double, y: Double): DoubleArray {
        var r0y = r0 - y;
        return doubleArrayOf(atan2(x, abs(r0y)) / n * sign(r0y), asin((c - (x * x + r0y * r0y) * n * n) / (2 * n)))

    }


    override fun project(lambda: Double, phi: Double): DoubleArray {

        var r = sqrt(c - 2 * n * sin(phi)) / n
        val lambdaN = lambda * n
        return doubleArrayOf(r * sin(lambda), r0 - r * cos(lambdaN));

    }

    override fun projectLambda(lambda: Double, phi: Double): Double {

        var r = sqrt(c - 2 * n * sin(phi)) / n
        val lambdaN = lambda * n
        return r * sin(lambda * n)

    }

    override fun projectPhi(lambda: Double, phi: Double): Double {

        var r = sqrt(c - 2 * n * sin(phi)) / n
        val lambdaN = lambda * n
        return r0 - r * cos(lambdaN)

    }
}