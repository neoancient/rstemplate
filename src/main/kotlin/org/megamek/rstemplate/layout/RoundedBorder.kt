package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.FILL_DARK_GREY
import org.megamek.rstemplate.svgNS
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

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
 */
class RoundedBorder (val x: Double, val y: Double, val width: Double, val height: Double,
                     val radius: Double, val control: Double, val strokeWidth: Double,
                     val stroke: String = FILL_DARK_GREY) {

    /**
     * Creates the path for the boder
     * @param doc The documennt that will contain the border
     * @return The border path element
     */
    fun draw(doc: Document): Element {
        val path = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)

        path.setAttributeNS(null, SVGConstants.CSS_FILL_PROPERTY, SVGConstants.SVG_NONE_VALUE)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, stroke)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.toString())
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M $x,${y + radius} c 0,-$control ${radius - control},-$radius $radius,-$radius"
                    + " l ${width - radius * 2},0 c $control,0 $radius,${radius - control}, $radius,$radius"
                    + " l 0,${height - radius * 2} c 0,$control ${control - radius},$radius, -$radius,$radius"
                    + " l -${width - radius * 2},0 c -$control,0 -$radius,${control - radius}, -$radius,-$radius"
                    + "Z"
        )

        return path
    }
}