package io.data2viz.geo.path

import io.data2viz.geo.noop
import io.data2viz.geo.noop2
import io.data2viz.geo.projection.Stream
import kotlin.math.abs


// TODO : check for use of D3 "adder" in PathArea and PathMeasure....
class PathArea : Stream {

    private var areaSum = .0
    private var areaRingSum = .0
    private var x00 = Double.NaN
    private var y00 = Double.NaN
    private var x0 = Double.NaN
    private var y0 = Double.NaN

    private var currentPoint: (Double, Double) -> Unit = noop2
    private var currentLineStart: () -> Unit = noop
    private var currentLineEnd: () -> Unit = noop

    fun result(): Double {
        val a = areaSum / 2.0
        areaSum = .0
        return a
    }

    override fun point(x: Double, y: Double, z: Double) = currentPoint(x, y)
    override fun lineStart() = currentLineStart()
    override fun lineEnd() = currentLineEnd()
    override fun polygonStart() {
        currentLineStart = ::areaRingStart
        currentLineEnd = ::areaRingEnd
    }

    override fun polygonEnd() {
        currentLineStart = noop
        currentLineEnd = noop
        currentPoint = noop2
        areaSum += abs(areaRingSum)
        areaRingSum = .0
    }

    private fun areaRingStart() {
        currentPoint = ::areaPointFirst
    }

    private fun areaPointFirst(x: Double, y: Double) {
        currentPoint = ::areaPoint
        x00 = x
        x0 = x
        y00 = y
        y0 = y
    }

    private fun areaPoint(x: Double, y: Double) {
        areaRingSum += y0 * x - x0 * y
        x0 = x
        y0 = y
    }

    private fun areaRingEnd() {
        areaPoint(x00, y00)
    }
}