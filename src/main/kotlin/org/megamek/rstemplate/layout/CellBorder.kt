package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FILL_DARK_GREY
import org.megamek.rstemplate.templates.FILL_WHITE
import org.megamek.rstemplate.templates.svgNS
import org.megamek.rstemplate.templates.truncate
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

/**
 *
 */

const val bevelX = 7.845
const val bevelY = 7.6
const val tabBevelX = 5.475
const val tabBevelY = 8.214
const val STROKE_WIDTH = 1.932

class CellBorder(val x: Double, val y: Double, val width: Double, val height: Double,
                 val textWidth: Double, val stroke: String = FILL_DARK_GREY,
                 val strokeWidth: Double = STROKE_WIDTH,
                 val topTab: Boolean = true,
                 val bottomTab: Boolean = false, val bevelTopLeft: Boolean = true,
                 val bevelTopRight: Boolean = true, val bevelBottomRight: Boolean = true,
                 val bevelBottomLeft: Boolean = true, val labelWidthBelow: Double? = null,
                 val equalBevels: Boolean = false) {

    fun draw(doc: Document): Element {
        val path = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)

        path.setAttributeNS(null, SVGConstants.CSS_FILL_PROPERTY,
            FILL_WHITE
        )
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, stroke)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.toString())
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE, calcPath())

        return path
    }

    private fun calcPath(): String {
        val cornerX = if (equalBevels) tabBevelX else bevelX
        val cornerY = if (equalBevels) tabBevelY else bevelY
        val sj = StringJoiner(" ")
        if (bevelTopLeft) {
            sj.add("M $x,${y + tabBevelY}")
            sj.add(absLineTo(tabBevelX, 0.0))
        } else {
            sj.add("M $x,$y")
        }
        if (topTab) {
            sj.add(relLineTo(textWidth, 0.0))
            sj.add(relLineTo(tabBevelX, tabBevelY))
            if (bevelTopRight) {
                sj.add(absLineTo(width - cornerX, tabBevelY))
                sj.add(relLineTo(cornerX, cornerY))
            } else {
                sj.add(absLineTo(width, tabBevelY))
            }
        } else {
            if (bevelTopRight) {
                sj.add(absLineTo(width - tabBevelX, 0.0))
                sj.add(relLineTo(tabBevelX, tabBevelY))
            } else {
                sj.add(absLineTo(width, 0.0))
            }
        }
        if (bottomTab) {
            sj.add(absLineTo(width, height))
            sj.add(relLineTo(-tabBevelX, tabBevelY))
            if (labelWidthBelow != null) {
                sj.add(absLineTo(labelWidthBelow + tabBevelX * 2 + padding, height + tabBevelY))
            } else {
                sj.add(absLineTo(width * 0.5, height + tabBevelY))
            }
            sj.add(relLineTo(-tabBevelX, -tabBevelY))
        } else if (bevelBottomRight) {
            sj.add(absLineTo(width, height - cornerY))
            sj.add(relLineTo(-cornerX, cornerY))
        } else {
            sj.add(absLineTo(width, height))
        }
        if (bevelBottomLeft) {
            sj.add(absLineTo(cornerX, height))
            sj.add(relLineTo(-cornerX, -cornerY))
        } else {
            sj.add(absLineTo(0.0, height))
        }
        sj.add("Z")
        return sj.toString()
    }

    private fun absLineTo(x: Double, y: Double): String {
        return "L ${(this.x + x).truncate()},${(this.y + y).truncate()}"
    }

    private fun relLineTo(x: Double, y: Double): String {
        return "l ${x.truncate()},${y.truncate()}"
    }
}