package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.FILL_DARK_GREY
import org.megamek.rstemplate.FONT_SIZE_VSMALL
import org.megamek.rstemplate.RecordSheet
import org.w3c.dom.Element

/**
 * Displays a label followed by a square box with rounded corners, optionally containing text.
 */
class DamageCheckBox(private val label: String, private val text: String? = null) {

    fun draw(sheet: RecordSheet, x: Double = 0.0, y: Double = 0.0, fontSize: Float, width: Double? = null,
             fontWeight: String = SVGConstants.SVG_NORMAL_VALUE, fill: String = FILL_DARK_GREY): Element {
        val g = sheet.createTranslatedGroup(x, y)
        val boxSize = sheet.calcFontHeight(fontSize).toDouble()
        val textLength = width?.minus(boxSize + sheet.calcTextLength("_", fontSize, fontWeight))
            ?: sheet.calcTextLength("${label}_", fontSize, fontWeight)
        sheet.addTextElement(0.0, boxSize, label, fontSize, fontWeight, fill, fixedWidth = true,
            width = textLength, parent = g)
        val box = RoundedBorder(textLength, 0.0, boxSize, boxSize, 1.315, 0.726,
            0.96, fill)
        g.appendChild(box.draw(sheet.document))
        if (text != null) {
            sheet.addTextElement(textLength + boxSize * 0.5,
                (boxSize + sheet.calcFontHeight(FONT_SIZE_VSMALL)) * 0.5 - 1,
                text, FONT_SIZE_VSMALL, fill = fill, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        }
        return g
    }
}