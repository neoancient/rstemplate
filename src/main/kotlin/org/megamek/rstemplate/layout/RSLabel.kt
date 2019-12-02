package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.*
import org.w3c.dom.Element

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
 */
class RSLabel (val sheet: RecordSheet, val x: Double, val y: Double, val text: String,
               val fontSize: Float, val bgColor: String = FILL_BLACK,
               val fgColor: String = FILL_WHITE, val center: Boolean = false){

    val textHeight = sheet.calcFontHeight(fontSize) * 0.625
    val textWidth = sheet.calcTextLength(text, fontSize, SVGConstants.SVG_BOLD_VALUE)
    val rectMargin = textWidth * 0.05
    val rectWidth = textWidth + rectMargin * 2.0
    val taperWidth = textHeight * tabBevelX / tabBevelY
    val labelWidth = rectWidth + taperWidth * 2

    fun height() = textHeight * 2

    fun draw(): Element {
        val xpos = if (center) {
            x - taperWidth - rectWidth * 0.5
        } else {
            x
        }
        val g = sheet.document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} ($xpos,$y)")
        val background = sheet.document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        background.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, bgColor)
        background.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M 0,${textHeight.truncate()} l ${taperWidth.truncate()},${(-textHeight).truncate()} l ${rectWidth.truncate()},0"
            + " l ${taperWidth.truncate()},${textHeight.truncate()} l ${(-taperWidth).truncate()},${textHeight.truncate()} l ${(-rectWidth).truncate()},0 Z")
        g.appendChild(background)

        val t = sheet.createTextElement(taperWidth + textWidth * 0.05,
            textHeight * 1.5, text, fontSize, SVGConstants.SVG_BOLD_VALUE, fgColor,
            fixedWidth = true)
        g.appendChild(t)

        return g
    }
}