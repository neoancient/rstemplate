package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.FILL_DARK_GREY
import org.megamek.rstemplate.FILL_LIGHT_GREY
import org.megamek.rstemplate.FILL_WHITE
import org.megamek.rstemplate.svgNS
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

/**
 *
 */

const val bevelX = 6.845
const val bevelY = 10.952
const val tabBevelX = 5.475
const val tabBevelY = 8.214
const val STROKE_WIDTH = 1.932

class CellBorder(val x: Double, val y: Double, val width: Double, val height: Double,
                 val textWidth: Double, val stroke: String = FILL_DARK_GREY,
                 val strokeWidth: Double = STROKE_WIDTH,
                 val bottomTab: Boolean = false,
                 val bevelTopRight: Boolean = true, val bevelBottomRight: Boolean = true,
                 val bevelBottomLeft: Boolean = true) {

    fun draw(doc: Document): Element {
        val path = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)

        path.setAttributeNS(null, SVGConstants.CSS_FILL_PROPERTY, FILL_WHITE)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, stroke)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.toString())
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE, calcPath())

        return path
    }

    private fun calcPath(): String {
        val sj = StringJoiner(" ")
        sj.add("M $x,${y + tabBevelY}")
        sj.add(absLineTo(tabBevelX, 0.0))
        sj.add(relLineTo(textWidth, 0.0))
        sj.add(relLineTo(tabBevelX, tabBevelY))
        if (bevelTopRight) {
            sj.add(absLineTo(width - bevelX, tabBevelY))
            sj.add(relLineTo(bevelX, bevelY))
        } else {
            sj.add(absLineTo(width, tabBevelY))
        }
        if (bottomTab) {
            sj.add(absLineTo(width, height))
            sj.add(relLineTo(-tabBevelX, tabBevelY))

            sj.add(absLineTo(width * 0.5, height + tabBevelY))
            sj.add(relLineTo(-tabBevelX, -tabBevelY))
        } else if (bevelBottomRight) {
            sj.add(absLineTo(width, height - bevelY))
            sj.add(relLineTo(-bevelX, bevelY))
        } else {
            sj.add(absLineTo(width, height))
        }
        if (bevelBottomLeft) {
            sj.add(absLineTo(bevelX, height))
            sj.add(relLineTo(-bevelX, -bevelY))
        } else {
            sj.add(absLineTo(0.0, height))
        }
        sj.add("Z")
        return sj.toString()
    }

    private fun absLineTo(x: Double, y: Double): String {
        return "L ${this.x + x},${this.y + y}"
    }

    private fun relLineTo(x: Double, y: Double): String {
        return "l $x,$y"
    }
}