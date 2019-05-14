package io.data2viz.geo.projection.polyhedral

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


fun matrix(a: Array<DoubleArray>, b: Array<DoubleArray>): DoubleArray {
    var u = subtract(a[1], a[0])
    var v = subtract(b[1], b[0])
    var phi = angle(u, v)
    var s = length(u) / length(v);

    return multiply(
        doubleArrayOf(
            1.0, 0.0, a[0][0],
            0.0, 1.0, a[0][1]
        ), multiply(
            doubleArrayOf(
                s, 0.0, 0.0,
                0.0, s, 0.0
            ), multiply(
                doubleArrayOf(
                    cos(phi), sin(phi), 0.0,
                    -sin(phi), cos(phi), 0.0
            ), doubleArrayOf(
                    1.0, 0.0, -b[0][0],
                    0.0, 1.0, -b[0][1]
                )
            )
        )
    );
}

//import {atan2, cos, sin, sqrt} from "../math";
//
//// Note: 6-element arrays are used to denote the 3x3 affine transform matrix:
//// [a, b, c,
////  d, e, f,
////  0, 0, 1] - this redundant row is left out.
//
//// Transform matrix for [a0, a1] -> [b0, b1].
//export default function(a, b) {
//    var u = subtract(a[1], a[0]),
//    v = subtract(b[1], b[0]),
//    phi = angle(u, v),
//    s = length(u) / length(v);
//
//    return multiply([
//        1, 0, a[0][0],
//        0, 1, a[0][1]
//    ], multiply([
//        s, 0, 0,
//        0, s, 0
//    ], multiply([
//        cos(phi), sin(phi), 0,
//        -sin(phi), cos(phi), 0
//    ], [
//        1, 0, -b[0][0],
//        0, 1, -b[0][1]
//    ])));
//}
//
//// Inverts a transform matrix.
fun inverse(m:DoubleArray):DoubleArray {
    var k = 1 / (m[0] * m[4] - m[1] * m[3]);
    return doubleArrayOf(
        k * m[4], -k * m[1], k * (m[1] * m[5] - m[2] * m[4]),
        -k * m[3], k * m[0], k * (m[2] * m[3] - m[0] * m[5])
    );
}
//
//// Multiplies two 3x2 matrices.
fun multiply(a: DoubleArray, b: DoubleArray): DoubleArray {
    return doubleArrayOf(
        a[0] * b[0] + a[1] * b[3],
        a[0] * b[1] + a[1] * b[4],
        a[0] * b[2] + a[1] * b[5] + a[2],
        a[3] * b[0] + a[4] * b[3],
        a[3] * b[1] + a[4] * b[4],
        a[3] * b[2] + a[4] * b[5] + a[5]
    )
}

// Subtracts 2D vectors.
fun subtract(a: DoubleArray, b: DoubleArray): DoubleArray {
    return doubleArrayOf(a[0] - b[0], a[1] - b[1])
}

fun angle(a: DoubleArray, b: DoubleArray): Double {
    return atan2(a[0] * b[1] - a[1] * b[0], a[0] * b[0] + a[1] * b[1]);
}

fun length(v: DoubleArray): Double {
    return sqrt(v[0] * v[0] + v[1] * v[1]);
}


//
//// Subtracts 2D vectors.
//function subtract(a, b) {
//    return [a[0] - b[0], a[1] - b[1]];
//}
//
//// Magnitude of a 2D vector.
//function length(v) {
//    return sqrt(v[0] * v[0] + v[1] * v[1]);
//}
//
//// Angle between two 2D vectors.
//function angle(a, b) {
//    return atan2(a[0] * b[1] - a[1] * b[0], a[0] * b[0] + a[1] * b[1]);
//}