package io.data2viz.geo.projection.polyhedral

// TODO generate on-the-fly to avoid external modification.
var octahedron = arrayOf(
    doubleArrayOf(0.0, 90.0),
    doubleArrayOf(-90.0, 0.0), doubleArrayOf(0.0, 0.0), doubleArrayOf(90.0, 0.0), doubleArrayOf(180.0, 0.0),
    doubleArrayOf(0.0, -90.0)
)

val default = arrayOf(
    doubleArrayOf(0.0, 2.0, 1.0),
    doubleArrayOf(0.0, 3.0, 2.0),
    doubleArrayOf(5.0, 1.0, 2.0),
    doubleArrayOf(5.0, 2.0, 3.0),
    doubleArrayOf(0.0, 1.0, 4.0),
    doubleArrayOf(0.0, 4.0, 3.0),
    doubleArrayOf(5.0, 4.0, 1.0),
    doubleArrayOf(5.0, 3.0, 4.0)
)

fun octahedronDefault() {

}
//
//export default [
//[0, 2, 1],
//[0, 3, 2],
//[5, 1, 2],
//[5, 2, 3],
//[0, 1, 4],
//[0, 4, 3],
//[5, 4, 1],
//[5, 3, 4]
//].map(function(face) {
//    return face.map(function(i) {
//        return octahedron[i];
//    });
//});