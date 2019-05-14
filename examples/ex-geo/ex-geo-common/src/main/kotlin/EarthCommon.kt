package io.data2viz.examples.geo

import io.data2viz.color.Colors
import io.data2viz.geo.clip.Clip
import io.data2viz.geo.clip.ClipCircle
import io.data2viz.geo.path.GeoPath
import io.data2viz.geo.path.geoPath
import io.data2viz.geo.polygonContainsCount
import io.data2viz.geo.projection.*
import io.data2viz.geojson.GeoJsonObject
import io.data2viz.math.deg
import io.data2viz.time.Date
import io.data2viz.viz.PathNode
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import kotlin.math.roundToInt


val allProjections = hashMapOf(
    "albers" to albersProjection(),
    "albersUSA" to alberUSAProjection() {
        scale = 500.0
    },
    "azimuthalEqualArea" to azimuthalEqualAreaProjection(),
    "azimuthalEquidistant" to azimuthalEquidistant(),
    "conicConformal" to conicConformalProjection(),
    "conicEqual" to conicEqualAreaProjection(),
    "conicEquidistant" to conicEquidistantProjection(),
    "equalEarth" to equalEarthProjection(),
    "equirectangular" to equirectangularProjection(),
    "gnomonic" to gnomonicProjection(),
    "identity" to identityProjection(),
    "mercator" to mercatorProjection(),
    "naturalEarth1" to naturalEarth1Projection(),
    "orthographic" to orthographicProjection(),
    "stereographic" to stereographicProjection(),
    "transverseMercator" to transverseMercatorProjection()
)
val allProjectionsNames = allProjections.keys.toList()

val allFiles = listOf(
    "world-110m.geojson",
    "world-110m-30percent.json",
    "world-110m-50percent.json",
    "world-110m-70percent.json"
)


val projectionsToSingleFile = hashMapOf(
    "albersUSA" to "us-states.json"
)


val defaultFileIndex = allFiles.indexOf("world-110m-30percent.json")
val defaultProjectionIndex = allProjectionsNames.indexOf("orthographic")


fun geoViz(world: GeoJsonObject, projectionName: String, vizWidth: Double = 960.0, vizHeight: Double = 700.0): Viz {


    val projectionOuter = allProjections[projectionName]
    projectionOuter!!.translate = doubleArrayOf(vizWidth / 2.0, vizHeight / 2.0)


    return viz {
        width = vizWidth
        height = vizHeight

        val fps = text {
            x = 10.0
            y = 40.0
            fill = Colors.Web.red
        }

        text {
            x = 10.0
            y = 60.0
            fill = Colors.Web.red
            textContent = projectionName
        }

        val angleText = text {
            x = 10.0
            y = 80.0
            fill = Colors.Web.red
            textContent = "Angle: 0"
        }


        val pathOuter = PathNode().apply {
            stroke = Colors.Web.black
            strokeWidth = 1.0
            fill = Colors.Web.whitesmoke
        }

        var geoPathOuter = geoPath(projectionOuter, pathOuter)

        geoPathOuter.path(world)
        add(pathOuter)

        val isNeedRotate = when (projectionName) {
            "albersUSA", "identity" -> false
            else -> true
        }


        if (isNeedRotate) {
            projectionOuter.rotate = arrayOf(103.0.deg, 0.0.deg, 0.0.deg)
        }

        animation { now: Double ->

            FPS.eventuallyUpdate(now)

            if (FPS.value >= 0) {
                fps.textContent = "Internal FPS: ${FPS.value.roundToInt()}"
            }


            if (isNeedRotate) {
                val angle = doRotate(geoPathOuter, pathOuter, world)
                angleText.textContent = "Angle: $angle"
            } else {
                angleText.textContent = "Angle: 0"
            }

        }

        onResize { newWidth, newHeight ->

            width = newWidth
            height = newHeight

            geoPathOuter = geoPath(projectionOuter, pathOuter)
        }


    }
}

private fun doRotate(
    geoPathOuter: GeoPath,
    pathOuter: PathNode,
    world: GeoJsonObject
): Double {
    val unixTime = Date().getTime()

    val rotate = geoPathOuter.projection.rotate
    var k = 60.0

    k  = 10.00


    var angle = ((unixTime % (360 * k)) / k).deg


    val koef = (angle.deg % 360) / 360



//    rotate[0] = (97 + 3.5 * koef).deg
    rotate[0] = (97.1 + 0.2 * koef).deg
//    rotate[0] = angle


    ClipCircle.countStartLine = 0
    ClipCircle.pointVisible = 0
    ClipCircle.intersectCount = 0
    ClipCircle.interpolateCount = 0
    ClipCircle.intersectsCount = 0
    ClipCircle.intersectCountA = 0
    ClipCircle.intersectCountB = 0
    ClipCircle.clipLineCount = 0
    ClipCircle.cnullCount = 0
    ClipCircle.beforecnullCount = 0
    ClipCircle.beforebeforecnullCount = 0
    ClipCircle.intersectsNotNullCount = 0
    ClipCircle.notbeforebeforecnullCount = 0
    ClipCircle.pointCount = 0
    ClipCircle.point0NullCount = 0
    ClipCircle.notbeforebeforecnullCountInner = 0
    ClipCircle.vTrueCount = 0
    Clip.polygonEndCount = 0
    Clip.polygonStartedCount = 0
    Clip.notEmptyCount = 0
    Clip.startInsideCount = 0

    Clip.baselineStart = 0
    Clip.baselineEnd = 0
    Clip.baselinePoint= 0
    Clip.baselinePolygonStart = 0
    Clip.baselinePolygonEnd = 0

    polygonContainsCount = 0
    pathOuter.clearPath()
    geoPathOuter.path(world)
    geoPathOuter.projection.rotate = rotate
        println("${Clip.baselineStart} ${Clip.baselineEnd} ${Clip.baselinePoint} ${Clip.baselinePolygonStart} ${Clip.baselinePolygonEnd}")

//    println("${ClipCircle.intersectCountA} ${ClipCircle.intersectCountB}")
//    println("clipLineCount = ${ClipCircle.clipLineCount}")
//    println("pointVisible = ${ClipCircle.pointVisible}")
//    println("vTrueCount = ${ClipCircle.vTrueCount}")
//    println("notbeforebeforecnullCountInner = ${ClipCircle.notbeforebeforecnullCountInner} point0NullCount = ${ClipCircle.point0NullCount} pointCount = ${ClipCircle.pointCount} notbeforebeforecnullCount = ${ClipCircle.notbeforebeforecnullCount} beforebeforecnullCount = ${ClipCircle.beforebeforecnullCount} beforecnullCount = ${ClipCircle.beforecnullCount} cnullCount ${ClipCircle.cnullCount} intersectsNotNullCount ${ClipCircle.intersectsNotNullCount}")
//    println("intersectsNotNullCount ${ClipCircle.intersectsNotNullCount}")
//    println("${ClipCircle.intersectCount} ${ClipCircle.interpolateCount} ${ClipCircle.intersectsCount} ${ClipCircle.clipLineCount} ")

//    println("clip ${Clip.polygonEndCount} ${Clip.polygonStartedCount} ${Clip.notEmptyCount} ${Clip.startInsideCount}")
//    println("preclip " + geoPathOuter.projection.preClip.)
//    println("polygonContainsCount " + polygonContainsCount)
//    println("ClipCircle.countStartLine " + ClipCircle.countStartLine + " pointVisible " + ClipCircle.pointVisible)
//    println("ClipCircle.countStartLine " + ClipCircle.countStartLine + " pointVisible " + ClipCircle.pointVisible)

    return rotate[0].deg
}

object FPS {
    val averageCount = 10
    var value = .0
    var count = 0
    var lastStart = Double.NaN

    /**
     * current: current time in ms.
     */
    fun eventuallyUpdate(current: Double) {
        if (lastStart == Double.NaN)
            lastStart = current
        if (count++ == averageCount) {
            val totalTime = current - lastStart
            value = 1.0e3 * averageCount / totalTime
            lastStart = current
            count = 0
        }
    }
}