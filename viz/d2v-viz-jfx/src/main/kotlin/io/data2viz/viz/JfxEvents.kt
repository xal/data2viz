package io.data2viz.viz


import io.data2viz.geom.Point
import javafx.event.Event
import javafx.event.EventType
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent


actual class KMouseDown {
    actual companion object MouseDownEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any =
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_PRESSED)
    }
}

actual class KMouseUp {
    actual companion object MouseUpEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any =
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_RELEASED)
    }
}

actual class KMouseEnter {
    actual companion object MouseEnterEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any =
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_ENTERED)
    }
}

actual class KMouseLeave {
    actual companion object MouseLeaveEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any =
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_EXITED)
    }
}

actual class KMouseOut {
    actual companion object MouseOutEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any
                = createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_EXITED_TARGET)
    }
}

actual class KMouseOver {
    actual companion object MouseOverEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any
            = createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_ENTERED_TARGET)

    }
}


actual class KMouseDoubleClick {
    actual companion object MouseDoubleClickEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any {

            val handler: (MouseEvent) -> Unit = { evt: MouseEvent ->
                if (evt.clickCount == 2) {
                    val kevent = evt.convertToKEvent()
                    listener(kevent)
                }
            }
            val jfxEvent = MouseEvent.MOUSE_CLICKED
            (target as Canvas).addEventHandler(jfxEvent, handler)
            return JvmEventHandle(jfxEvent, handler)
        }
    }
}

actual class KMouseMove {
    actual companion object MouseMoveEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit) {
            // Add listeners for both events MOVED & DRAGGED, because MOVED not fires when any button pressed
            // but JS behaviour is different
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_MOVED)
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_DRAGGED)
        }
    }
}

actual class KMouseClick {
    actual companion object MouseClickEventListener : KEventListener<KMouseEvent> {
        override fun addNativeListener(target: Any, listener: (KMouseEvent) -> Unit): Any =
            createSimpleJvmEventHandle(listener, target, MouseEvent.MOUSE_CLICKED)
    }
}

private fun createSimpleJvmEventHandle(
    listener: (KMouseEvent) -> Unit,
    target: Any,
    jfxEvent: EventType<MouseEvent>
): JvmEventHandle<MouseEvent> {
    val handler: (MouseEvent) -> Unit = { evt: MouseEvent ->
        val kevent = evt.convertToKEvent()
        listener(kevent)
    }
    (target as Canvas).addEventHandler(jfxEvent, handler)
    return JvmEventHandle(jfxEvent, handler)
}

data class JvmEventHandle<T : Event?>(val type: EventType<T>, val handler: (MouseEvent) -> Unit)

/**
 * Add an event listener on a a viz.
 * @return an handler to eventually remove later.
 */
actual fun <T> Viz.on(
    eventListener: KEventListener<T>,
    listener: (T) -> Unit
): Any {
    val jFxVizRenderer = this.renderer as JFxVizRenderer
    return eventListener.addNativeListener(jFxVizRenderer.canvas, listener)
}


/**
 *
 */
private fun MouseEvent.convertToKEvent(): KMouseEvent {
    val kMouseMoveEvent = KMouseEvent(
        Point(x, y),
        this.isAltDown,
        this.isControlDown,
        this.isShiftDown,
        this.isMetaDown
    )
    return kMouseMoveEvent
}