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
               val fontSize: Double, val bgColor: String = FILL_BLACK,
               val fgColor: String = FILL_WHITE, val center: Boolean = false){

    val textHeight = sheet.calcFontHeight(fontSize.toFloat()) * 0.625
    val textWidth = sheet.calcTextLength(text, fontSize.toFloat(), SVGConstants.SVG_BOLD_VALUE)
    val rectMargin = textWidth * 0.05
    val rectWidth = textWidth + rectMargin * 2.0
    val taperWidth = textHeight * tabBevelX / tabBevelY
    val labelWidth = rectWidth + taperWidth * 2

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
            """M 0,$textHeight
            l $taperWidth,-$textHeight
            l $rectWidth,0
            l $taperWidth,$textHeight
            l -$taperWidth,$textHeight
            l -$rectWidth,0 Z""".trimIndent())
        g.appendChild(background)

        val t = sheet.document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        t.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, TYPEFACE)
        t.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, fontSize.toString())
        t.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, SVGConstants.SVG_BOLD_VALUE)
        t.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, (taperWidth + textWidth * 0.05).toString())
        t.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, (textHeight * 1.5).toString())
        t.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fgColor)
        t.textContent = text
        g.appendChild(t)

        return g
    }
}