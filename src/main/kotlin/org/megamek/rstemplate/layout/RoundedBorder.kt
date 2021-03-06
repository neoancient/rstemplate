package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FILL_DARK_GREY
import org.megamek.rstemplate.templates.svgNS
import org.megamek.rstemplate.templates.truncate
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Creates a rectangular border with rounded corners
 * @param x The x coordinate of the top left
 * @param y The y coordinate of the top left
 * @param width The width of the rectangle
 * @param height The height of the rectangle
 * @param radius The radius of the curve at the corner
 * @param control The length of the control point segments
 * @param strokeWidth The width of the border line
 * @param stroke The color of the border
 * @param fill The color of in the interior
 */
class RoundedBorder (val x: Double, val y: Double, val width: Double, val height: Double,
                     val radius: Double, val control: Double, val strokeWidth: Double,
                     val stroke: String = FILL_DARK_GREY, val fill: String = SVGConstants.SVG_NONE_VALUE
) {

    /**
     * Creates the path for the boder
     * @param doc The documennt that will contain the border
     * @return The border path element
     */
    fun draw(doc: Document): Element {
        val path = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)

        path.setAttributeNS(null, SVGConstants.CSS_FILL_PROPERTY, fill)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, stroke)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.toString())
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${x.truncate()},${(y + radius).truncate()}"
                    + " c 0,${(-control).truncate()} ${(radius - control).truncate()},${(-radius).truncate()} ${radius.truncate()},${(-radius).truncate()}"
                    + " l ${(width - radius * 2).truncate()},0 c ${control.truncate()},0 ${radius.truncate()},${(radius - control).truncate()}, ${radius.truncate()},${radius.truncate()}"
                    + " l 0,${(height - radius * 2).truncate()} c 0,${control.truncate()} ${(control - radius).truncate()},${radius.truncate()}, ${(-radius).truncate()},${radius.truncate()}"
                    + " l -${(width - radius * 2).truncate()},0 c ${(-control).truncate()},0 ${(-radius).truncate()},${(control - radius).truncate()}, ${(-radius).truncate()},${(-radius).truncate()}"
                    + "Z"
        )

        return path
    }
}