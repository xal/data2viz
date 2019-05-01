package io.data2viz.viz

import io.data2viz.geom.Point
import kotlin.math.pow
import kotlin.math.sqrt


fun <T> VizRenderer.addEventHandle(handle: KEventHandle<T>) where T : io.data2viz.viz.KEvent {

    if(handle.isAddedToRenderer) {
        error("Can't add event handle which already added to Renderer")
    }

    handle.disposable = addNativeEventListenerFromHandle(handle)
}

expect fun <T> VizRenderer.addNativeEventListenerFromHandle(handle: KEventHandle<T>): Disposable where T : KEvent

fun <T> VizRenderer.removeEventHandle(handle: KEventHandle<T>) where T : io.data2viz.viz.KEvent {

    if(!handle.isAddedToRenderer) {
        error("Can't remove event handle which not added to Renderer. $handle")
    }

    handle.disposable!!.dispose()
    handle.disposable = null
}

/**
 * TODO: Make generic disposable class in API?
 */
interface Disposable {
    fun dispose()
}

class CompositeDisposable(val disposables: MutableList<Disposable> = mutableListOf()): Disposable {
    override fun dispose() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

}

/**
 * Marker interface on events.
 */
interface KEvent


class KEventHandle<T>(
    val eventListener: KEventListener<T>,
    val listener: (T) -> Unit,
    val onDispose: (KEventHandle<T>) -> Unit
): Disposable where T : KEvent {

    var disposable: Disposable? = null

    val isAddedToRenderer get() =  disposable != null

    override fun dispose() {
        onDispose(this)
    }

    override fun toString(): String {
        return "KEventHandle(eventListener=$eventListener)"
    }
}

/**
 * Common Pointer event. Can be subclassed into more specific events.
 * Gives access to the position of the event.
 */
open class KPointerEvent(
    val pos: Point
) : KEvent {
    override fun toString(): String = "KPointerEvent(pos=$pos)"
}

/**
 * Pointer events for platform with Mouse input device.
 * Somebody may want use KMouseEvent by casting KPointerEvent to more specific type
 * Used in JFX & JS implementations. Android implementation use common KPointerEvent
 */
class KMouseEvent(
    pos: Point,
    val altKey: Boolean,
    val ctrlKey: Boolean,
    val shiftKey: Boolean,
    val metaKey: Boolean
) : KPointerEvent(pos) {
    override fun toString(): String = "KMouseEvent(pos=$pos)"
}

class KDragEvent(
    val action: KDragAction,
    val pointerEvent: KPointerEvent
) : KEvent {
    val pos get() = pointerEvent.pos
    override fun toString(): String = "KDragEvent(action=$action, pos=$pos)"

    enum class KDragAction {
        Start, Dragging, Finish
    }
}


interface KEventListener<T> where  T : KEvent {
    fun addNativeListener(target: Any, listener: (T) -> Unit): Disposable
}

expect class KPointerMove {
    companion object MouseMoveEventListener : KEventListener<KPointerEvent>
}

expect class KPointerDown {
    companion object MouseDownEventListener : KEventListener<KPointerEvent>
}

expect class KPointerUp {
    companion object MouseUpEventListener : KEventListener<KPointerEvent>
}

expect class KPointerEnter {
    companion object MouseEnterEventListener : KEventListener<KPointerEvent>
}

expect class KPointerLeave {
    companion object MouseLeaveEventListener : KEventListener<KPointerEvent>
}

expect class KPointerClick {
    companion object MouseClickEventListener : KEventListener<KPointerEvent>
}

expect class KPointerDoubleClick {
    companion object MouseDoubleClickEventListener : KEventListener<KPointerEvent>
}


class KPointerDrag {
    companion object MouseDragEventListener : KEventListener<KDragEvent> {

        const val minDistanceForDetectDragging = 100

        private var downActionPos: Point? = null
        private var dragInProgress: Boolean = false

        override fun addNativeListener(target: Any, listener: (KDragEvent) -> Unit): Disposable {

            val compositeDisposable = CompositeDisposable();

            compositeDisposable.add(KPointerMove.addNativeListener(target) {
                if (dragInProgress) {
                    listener(KDragEvent(KDragEvent.KDragAction.Dragging, it))
                } else {

                    val startPos = downActionPos
                    if (startPos != null) {

                        val distance = distance(startPos, it.pos)

                        if (distance > minDistanceForDetectDragging) {
                            dragInProgress = true
                            listener(KDragEvent(KDragEvent.KDragAction.Start, it))
                        }
                    }
                }
            })

            compositeDisposable.add(KPointerLeave.addNativeListener(target) {
                onDragNotPossible(listener, it)
            })

            compositeDisposable.add(KPointerDown.addNativeListener(target) {
                downActionPos = it.pos

            })

            compositeDisposable.add(KPointerUp.addNativeListener(target) {
                onDragNotPossible(listener, it)
            })

            return compositeDisposable
        }

        /**
         * TODO: Move to API?
         */
        private fun distance(pos1: Point, pos2: Point): Double {
            val xSquare = (pos1.x - pos2.x).pow(2.0)
            val ySquare = (pos1.y - pos2.y).pow(2.0)
            return sqrt(xSquare + ySquare)
        }

        private fun onDragNotPossible(listener: (KDragEvent) -> Unit, motionEvent: KPointerEvent) {
            downActionPos = null
            if (dragInProgress) {
                dragInProgress = false
                listener(KDragEvent(KDragEvent.KDragAction.Finish, motionEvent))
            }

        }
    }
}

