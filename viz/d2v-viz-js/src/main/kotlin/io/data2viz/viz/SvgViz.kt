package io.data2viz.viz

import org.w3c.dom.Element
import org.w3c.dom.svg.SVGElement
import kotlin.browser.document
import kotlin.reflect.KProperty


val svgNamespaceURI = "http://www.w3.org/2000/svg"
internal fun createSVGElement(name: String) = document.createElementNS(svgNamespaceURI, name)

fun selectOrCreateSvg(): SVGElement {
    var svgElement = document.querySelector("svg") as SVGElement?
    if (svgElement == null) {
        svgElement = createSVGElement("svg") as SVGElement
        document.querySelector("body")!!.append(svgElement)
    }
    return svgElement
}


/**
 * Bootstrap a VizContext on a existing SVG element.
 */
fun Element.viz(init: SVGVizContext.() -> Unit): VizContext {
    val context = SVGVizContext(this)
    init(context)
    return context
}

class SVGVizContext(val parent: Element) : VizContext {

    init {
        check(parent.namespaceURI == svgNamespaceURI)
    }

    override fun circle(init: CircleVizItem.() -> Unit): CircleVizItem {

        val circle = circle()
        init(circle)
        parent.append(circle.element)
        return circle
    }

}


internal fun circle() = CircleElement(createSVGElement("circle"))

interface AccessByAttributes {
    fun setAttribute(name: String, value: String?)
    fun getAttribute(name: String): String?
}

interface ElementWrapper : AccessByAttributes {
    val element: Element

    override fun setAttribute(name: String, value: String?) {
        if (value != null)
            element.setAttribute(name, value)
        else
            element.removeAttribute(name)
    }

    override fun getAttribute(name: String) = element.getAttribute(name)

}

//@SvgTagMarker
class CircleElement(override val element: Element) : ElementWrapper, CircleVizItem {
    override var cx: Double by DoubleAttributePropertyDelegate()
    override var cy: Double by DoubleAttributePropertyDelegate()
    override var radius: Double by DoubleAttributePropertyDelegate()
}


val propertyMapping = mapOf(
        "radius" to "r"
)

class DoubleAttributePropertyDelegate {
    operator fun getValue(elementWrapper: ElementWrapper, property: KProperty<*>): Double =
            elementWrapper.element.getAttribute(propertyMapping.getOrElse(property.name, {property.name}))?.toDouble() ?: 0.0

    operator fun setValue(element: ElementWrapper, property: KProperty<*>, d: Double) {
        element.element.setAttribute(propertyMapping.getOrElse(property.name, {property.name}), d.toString())
    }

}

