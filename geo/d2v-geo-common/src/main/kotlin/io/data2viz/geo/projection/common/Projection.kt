package io.data2viz.geo.projection.common


import io.data2viz.geo.geometry.clip.StreamPostClip
import io.data2viz.geo.geometry.clip.StreamPreClip
import io.data2viz.geo.projection.common.IdentityRotationProjector.projectLambda
import io.data2viz.geo.projection.common.IdentityRotationProjector.projectPhi
import io.data2viz.geo.stream.Stream
import io.data2viz.math.Angle


/**
 * Todo document
 */
interface Projector {

    /**
     * Project a stream point
     * Internal API usually call projectLambda & projectPhi separately to avoid new Double array allocation
     */
    fun project(lambda: Double, phi: Double): DoubleArray

    /**
     * Default implementation of a longitude projection (can be overrided)
     * Required for performance (avoid allocating new Double array on each project)
     * see project(lambda: Double, phi: Double)
     */
    fun projectLambda(lambda: Double, phi: Double): Double

    /**
     * Default implementation of a latitude projection (can be overrided)
     * Required for performance (avoid allocating new Double array on each project)
     * see project(lambda: Double, phi: Double)
     */
    fun projectPhi(lambda: Double, phi: Double): Double

    /**
     * TODO docs
     */
    fun invertLambda(lambda: Double, phi: Double): Double


    /**
     * TODO docs
     */
    fun invertPhi(lambda: Double, phi: Double): Double


    /**
     * Returns a new array [longitude, latitude] in degrees representing the unprojected point of the given projected point.
     * The point must be specified as a two-element array [translateX, translateY] (typically in pixels).
     * Todo document
     * May return null if the specified point has no defined projected position, such as when the point is outside the clipping bounds of the projection.
     */
    fun invert(lambda: Double, phi: Double): DoubleArray

}



/**
 * Todo document
 */
interface Projection : Projector {
    /**
     * The scale factor corresponds linearly to the distance between projected points;
     * however, absolute scale factors are not equivalent across projections.
     */
    var scale: Double

    /**
     ** TODO: check with translate
     * Determines the pixel coordinates of the projection’s center by X axys
     */
    var translateX: Double
    /**
     ** TODO: check with translate
     * Determines the pixel coordinates of the projection’s center by Y axys
     */
    var translateY: Double


    var centerLat: Angle
    var centerLon: Angle
    /**
     * The threshold for the projection’s adaptive resampling pixels.
     * This value corresponds to the Douglas–Peucker distance.
     * Defaults to √0.5 ≅ 0.70710…
     */
    var precision: Double

    var rotateLambda: Angle
    var rotatePhi: Angle
    var rotateGamma: Angle

    /**
     * TODO: check
     * If preclip is specified, sets the projection’s spherical clipping to the specified function and returns the projection. If preclip is not specified, returns the current spherical clipping function (see preclip).
     */
    var preClip: StreamPreClip

    /**
     * TODO: check
     * If postclip is specified, sets the projection’s cartesian clipping to the specified function and returns the projection. If postclip is not specified, returns the current cartesian clipping function (see postclip).
     */
    var postClip: StreamPostClip


    /**
     * TODO: check
     * Returns a projection stream for the specified output stream.
     * Any input geometry is projected before being streamed to the output stream.
     * A typical projection involves several geometry transformations:
     * the input geometry is first converted to radians, rotated on three axes,
     * clipped to the small circle or cut along the antimeridian, and lastly projected to the plane
     * with adaptive resampling, scale and translation.
     */
    fun stream(stream: Stream): Stream


    /**
     * TODO: check
     * The translation offset determines the pixel coordinates of the projection’s center.
     * The default translation offset places ⟨0°,0°⟩ at the center of a 960×500 area.
     * It is equivalent for translateX = 0 & translateY = 0 but with better performance
     */
    fun translate(x: Double, y: Double)


    /**
     * a two-element array of longitude and latitude in degrees
     */
    fun center(lat: Angle, lon: Angle)

    /**
     * The projection’s three-axis spherical rotation to
     * the specified angles, which must be a two- or three-element array of numbers [lambda, phi, gamma]
     * specifying the rotation angles in degrees about each spherical axis
     * (these correspond to yaw, pitch and roll).
     */
    fun rotate(lambda: Angle, phi: Angle, gamma: Angle? = null)


}




interface NoInvertProjector: Projector {

    override fun invert(lambda: Double, phi: Double) =
        invertError() as DoubleArray

    override fun invertLambda(lambda: Double, phi: Double): Double =
        invertError() as Double


    /**
     * TODO docs
     */
    override fun invertPhi(lambda: Double, phi: Double): Double =
        invertError() as Double

    private fun invertError(): Any {
        error("$this don't support invert operation")
    }
}


interface SimpleProjector : Projector {

    override fun projectLambda(lambda: Double, phi: Double): Double = project(lambda, phi)[0]

    override fun projectPhi(lambda: Double, phi: Double): Double = project(lambda, phi)[1]

    override fun invertLambda(lambda: Double, phi: Double): Double = invert(lambda, phi)[0]

    override fun invertPhi(lambda: Double, phi: Double): Double = invert(lambda, phi)[1]

}


interface NoCommonCalculationsProjector : Projector {

    override fun project(lambda: Double, phi: Double) =  
            doubleArrayOf(
                projectLambda(lambda, phi),
                projectPhi(lambda, phi)
            )

    override fun invert(lambda: Double, phi: Double) =
        doubleArrayOf(
            invertLambda(lambda, phi),
            invertPhi(lambda, phi)
        )
}

interface SimpleNoInvertProjector : NoInvertProjector {

    override fun projectLambda(lambda: Double, phi: Double): Double = project(lambda, phi)[0]

    override fun projectPhi(lambda: Double, phi: Double): Double = project(lambda, phi)[1]

}

