package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.*
import org.w3c.dom.Element
import java.lang.Double.min

/**
 * Adds a text label with diamonds on the sides
 *
 * @param sheet    The record sheet, needed to calculate font metrics
 * @param x        The x position of the label in the document
 * @param y        The y position of the label in the document
 * @param text     The text to place in the label
 * @param fontSize The size of font to use in the label. The label will be sized proportionately with the font
 * @param bgColor  The background color of the label
 * @param fgColor  The foreground (text) color of the label
 * @param center   If true, the center of the label will be at the x position
 * @param right    If true, the label will be right justified. If center is also true, this is ignored.
 */
class RSLabel (val sheet: RecordSheet, val x: Double, val y: Double, val text: String,
               val fontSize: Float, val bgColor: String = FILL_BLACK,
               val fgColor: String = FILL_WHITE, val center: Boolean = false,
               val right: Boolean = false, val width: Double? = null, val fixedWidth: Boolean = true,
               val textId: String? = null, val centerText: Boolean = true,
               val bevelX: Double = tabBevelX, val bevelY: Double = tabBevelY) {

    val textHeight = sheet.calcFontHeight(fontSize) * 0.625
    val textWidth = sheet.calcTextLength(text, fontSize, SVGConstants.SVG_BOLD_VALUE)
    val rectMargin = textWidth * 0.05
    val taperWidth = textHeight * this.bevelX / this.bevelY
    val rectWidth = width ?: textWidth + rectMargin * 2

    fun height() = textHeight * 2

    fun draw(): Element {
        val xpos = if (center) {
            x - taperWidth - rectWidth * 0.5
        } else if (right) {
            x - taperWidth - rectWidth
        } else {
            x
        }
        val g = sheet.document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} ($xpos,$y)")
        val background = sheet.document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        background.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, bgColor)
        background.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M 0,${textHeight.truncate()} l ${taperWidth.truncate()},${(-textHeight).truncate()} h ${rectWidth.truncate()}"
            + " l ${taperWidth.truncate()},${textHeight.truncate()} l ${(-taperWidth).truncate()},${textHeight.truncate()} h ${(-rectWidth).truncate()} Z")
        g.appendChild(background)

        val t = sheet.createTextElement(if (centerText) taperWidth + rectWidth * 0.5 else taperWidth + padding,
            textHeight * 1.5, text, fontSize, SVGConstants.SVG_BOLD_VALUE, fill = fgColor,
            anchor = if (centerText) SVGConstants.SVG_MIDDLE_VALUE else SVGConstants.SVG_START_VALUE,
            fixedWidth = fixedWidth, width = if (fixedWidth) min(rectWidth - rectMargin * 2, textWidth * 1.5)
            else rectWidth - rectMargin * 2, id = textId)
        g.appendChild(t)

        return g
    }
}