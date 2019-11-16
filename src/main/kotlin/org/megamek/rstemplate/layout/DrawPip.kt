package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.FILL_DARK_GREY
import org.megamek.rstemplate.svgNS
import org.w3c.dom.Document
import org.w3c.dom.Element

// Ratio of distance from end point to control point to the radius.
private const val CONST_C = 0.55191502449
// Format String for writing a curve to a path definition attribute
private const val FMT_CURVE = " c %f %f,%f %f,%f %f"

/**
 * Approximates a circle using four Bezier curves.
 *
 * @param x      Position of left of bounding rectangle.
 * @param y      Position of top of bounding rectangle.
 * @param radius Radius of the circle
 */
class DrawPip(val x: Double, val y: Double, val radius: Double, val strokeWidth: Double) {
    fun draw(document: Document): Element {
        val path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, strokeWidth.toString())

        // Move to start of pip, at (1, 0)
        val d = StringBuilder("M").append(x + radius * 2).append(",").append(y + radius)
        // c is the length of each control line
        val c = CONST_C * radius

        // Draw arcs anticlockwise. The coordinates are relative to the beginning of the arc.
        d.append(String.format(FMT_CURVE, 0.0, -c, c - radius, -radius, -radius, -radius))
        d.append(String.format(FMT_CURVE, -c, 0.0, -radius, radius - c, -radius, radius))
        d.append(String.format(FMT_CURVE, 0.0, c, radius - c, radius, radius, radius))
        d.append(String.format(FMT_CURVE, c, 0.0, radius, c - radius, radius, -radius))

        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE, d.toString())
        return path
    }
}