package io.data2viz.geo.clip

import io.data2viz.geo.*
import io.data2viz.geo.projection.Stream
import io.data2viz.math.EPSILON
import io.data2viz.math.PI
import io.data2viz.math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt


fun clipCircle(radius: Double) = { stream: Stream -> Clip(ClipCircle(radius), stream) }
//clip(visible, clipLine, interpolate, smallRadius ? [0, -radius] : [-pi, radius - pi]);

/**
 * Generates a clipping function which transforms a stream such that geometries are bounded by a small circle of
 * radius angle around the projection’s center.
 * Typically used for pre-clipping.
 */
class ClipCircle(val radius: Double) : ClippableHasStart {

    companion object {

        var countStartLine = 0
        var pointVisible = 0
        var intersectCount = 0
        var intersectCountA = 0
        var intersectCountB = 0

        var interpolateCount = 0
        var intersectsCount = 0
        var intersectsNotNullCount = 0
        var cnullCount = 0
        var beforecnullCount = 0
        var beforebeforecnullCount = 0
        var vTrueCount = 0
        var notbeforebeforecnullCount = 0
        var notbeforebeforecnullCountInner = 0
        var pointCount = 0
        var point0NullCount = 0
        var clipLineCount = 0
    }

    private val cr = cos(radius)
    private val delta = 6.0.toRadians()
    private val smallRadius = cr > 0
    private val notHemisphere = abs(cr) > EPSILON // TODO optimise for this common case

    override val start: DoubleArray
        get() = if (smallRadius) doubleArrayOf(0.0, -radius) else doubleArrayOf(-PI, radius - PI)


    init {
//        println("Clip $radius $cr $delta $smallRadius $notHemisphere")
    }

    override fun pointVisible(x: Double, y: Double): Boolean {
        val b = cos(x) * cos(y) > cr
        if(b) {
            pointVisible++
        }
        return b
    }

    override fun clipLine(stream: Stream): ClipStream {
        clipLineCount++
        return object : ClipStream {

            private var _clean = 0
            private var point0: DoubleArray? = null             // previous point
            private var c0 = 0                                  // code for previous point
            private var v0 = false                              // visibility of previous point
            private var v00 = false                             // visibility of first point

            override var clean: Int = 0
                get() = _clean or ((if (v00 && v0) 1 else 0) shl 1)

            override fun point(x: Double, y: Double, z: Double) {
                pointCount++
                val point1 = doubleArrayOf(x, y)
                var point2: DoubleArray?
                var v = pointVisible(x, y)
                val c = if (smallRadius) {
                    if (v) 0 else code(x, y)
                } else {
                    if (v) code(x + (if (x < 0) PI else -PI), y) else 0
                }
                if (point0 == null) {
                    point0NullCount++
                    v00 = v
                    v0 = v
                    if (v) stream.lineStart()
                }

                // Handle degeneracies.
                // TODO ignore if not clipping polygons.
                if (v != v0) {
                    notbeforebeforecnullCount++
                    point2 = intersect(point0!!, point1)
                    if (point2 == null || pointEqual(point0!!, point2) || pointEqual(point1, point2)) {
                        notbeforebeforecnullCountInner++
                        point1[0] += EPSILON
                        point1[1] += EPSILON
                        v = pointVisible(point1[0], point1[1])
                    }
                }

                if (v != v0) {
                    beforebeforecnullCount++
                    _clean = 0
                    if (v) {
                        vTrueCount++;
                        // outside going in
                        stream.lineStart()
                        point2 = intersect(point1, point0!!)
                        stream.point(point2!![0], point2[1], .0)            // TODO : point2 may be null ??
                    } else {
                        // inside going out
                        point2 = intersect(point0!!, point1)
                        stream.point(point2!![0], point2[1], .0)            // TODO : point2 may be null ??
                        stream.lineEnd()
                    }
                    point0 = point2
                } else if (notHemisphere && point0 != null && smallRadius xor v) {
                    beforecnullCount++
                    // If the codes for two points are different, or are both zero,
                    // and there this segment intersects with the small circle.
//                    println("c = $c c0 = $c0")
                    if ((c and c0) == 0) {
                        cnullCount++
                        val t = intersects(point1, point0!!)

                        if (t != null) {
                            intersectsNotNullCount++
                            _clean = 0
                            if (smallRadius) {
                                stream.lineStart()
                                stream.point(t[0][0], t[0][1], .0)
                                stream.point(t[1][0], t[1][1], .0)
                                stream.lineEnd()
                            } else {
                                stream.point(t[1][0], t[1][1], .0)
                                stream.lineEnd()
                                stream.lineStart()
                                stream.point(t[0][0], t[0][1], .0)
                            }
                        }
                    }
                }

                if (v && (point0 == null || !pointEqual(point0!!, point1))) {
                    stream.point(point1[0], point1[1], .0)
                }
                point0 = point1
                v0 = v
                c0 = c
            }


            override fun lineStart() {
                countStartLine++

                v00 = false
                v0 = false
                _clean = 1
            }

            override fun lineEnd() {
                if (v0) stream.lineEnd()
                point0 = null
            }
        }

    }

    override fun interpolate(from: DoubleArray?, to: DoubleArray?, direction: Int, stream: Stream) {
        interpolateCount++
        geoCircle(stream, radius, delta, direction, from, to)
    }

    // Intersects the great circle between a and b with the clip circle.
    private fun intersect(a: DoubleArray, b: DoubleArray): DoubleArray? {
        intersectCount++
        val pa = cartesian(a)
        val pb = cartesian(b)

        // We have two planes, n1.p = d1 and n2.p = d2.
        // Find intersection line p(t) = c1 n1 + c2 n2 + t (n1 ⨯ n2).
        val n1 = doubleArrayOf(1.0, .0, .0)                 // normal
        val n2 = cartesianCross(pa, pb)
        val n2n2 = cartesianDot(n2, n2)
        val n1n2 = n2[0]                                        // cartesianDot(n1, n2)
        val determinant = n2n2 - n1n2 * n1n2

        //if (!determinant) return !two && a;
        // Two polar points.
        if (determinant == .0) return a
        intersectCountA++

        val c1 = cr * n2n2 / determinant
        val c2 = -cr * n1n2 / determinant
        val n1xn2 = cartesianCross(n1, n2)
        var A = cartesianScale(n1, c1)
        val B = cartesianScale(n2, c2)
        A = cartesianAdd(A, B)

        // Solve |p(t)|^2 = 1
        val u = n1xn2
        val w = cartesianDot(A, u)
        val uu = cartesianDot(u, u)
        val t2 = w * w - uu * (cartesianDot(A, A) - 1)

        if (t2 < 0) return null

        intersectCountB++

        val t = sqrt(t2)
        var q = cartesianScale(u, (-w - t) / uu)
        q = cartesianAdd(q, A)
        q = spherical(q)

        return q
    }

    // TODO : factorize with intersect !
    private fun intersects(a: DoubleArray, b: DoubleArray): Array<DoubleArray>? {
        intersectsCount++
        val pa = cartesian(a)
        val pb = cartesian(b)

        // We have two planes, n1.p = d1 and n2.p = d2.
        // Find intersection line p(t) = c1 n1 + c2 n2 + t (n1 ⨯ n2).
        val n1 = doubleArrayOf(1.0, .0, .0)                 // normal
        val n2 = cartesianCross(pa, pb)
        val n2n2 = cartesianDot(n2, n2)
        val n1n2 = n2[0]                                        // cartesianDot(n1, n2)
        val determinant = n2n2 - n1n2 * n1n2

        // Two polar points.
        if (determinant == .0 || determinant == Double.NaN) return null

        val c1 = cr * n2n2 / determinant
        val c2 = -cr * n1n2 / determinant
        val n1xn2 = cartesianCross(n1, n2)
        var A = cartesianScale(n1, c1)
        val B = cartesianScale(n2, c2)
        A = cartesianAdd(A, B)

        // Solve |p(t)|^2 = 1
        val u = n1xn2
        val w = cartesianDot(A, u)
        val uu = cartesianDot(u, u)
        val t2 = w * w - uu * (cartesianDot(A, A) - 1)

        if (t2 < 0) return null

        val t = sqrt(t2)
        var q = cartesianScale(u, (-w - t) / uu)
        q = cartesianAdd(q, A)
        q = spherical(q)

        // Two intersection points.
        var lambda0 = a[0]
        var lambda1 = b[0]
        var phi0 = a[1]
        var phi1 = b[1]

        if (lambda1 < lambda0) {
            val z = lambda0
            lambda0 = lambda1
            lambda1 = z
        }

        val delta = lambda1 - lambda0
        val polar = abs(delta - PI) < EPSILON
        val meridian = polar || delta < EPSILON

        if (!polar && phi1 < phi0) {
            val z = phi0
            phi0 = phi1
            phi1 = z
        }

        // Check that the first point is between a and b.
        val test = if (meridian) {
            if (polar) {

                val t  = (phi0 + phi1 > 0) xor (q[1] < if (abs(q[0] - lambda0) < EPSILON) phi0 else phi1)

                if(t) println("1")
                t
            } else {

                val t  =(phi0 <= q[1] && q[1]  <= phi1)
                if(t) {
//                    println("determinant $determinant")
//                    println("2tt1 ${a[0]} ${a[1]} ${b[0]} ${b[1]} cr =$cr")
//                    println("2ttt $phi0 ${q[1]} $phi1")
                }

                t
            }
        } else {
            val t =(delta > PI) xor (q[0] in lambda0..lambda1)
            if(t) println("3")
            t
        }


//        println("polar = $polar meridian = $meridian delta=$delta $lambda1 $lambda0")
        if (test) {

            var q1 = cartesianScale(u, (-w + t) / uu)
            q1 = cartesianAdd(q1, A)
            return arrayOf(q, spherical(q1))
        }
        return null
    }

    // Generates a 4-bit vector representing the location of a point relative to
    // the small circle's bounding box.
    fun code(x: Double, y: Double): Int {
        val r = if (smallRadius) radius else PI - radius
        var code = 0
        if (x < -r) code = code or 1               // left
        else if (y > r) code = code or 2           // right
        if (y < -r) code = code or 4               // below
        else if (y > r) code = code or 8           // above
        return code
    }
}