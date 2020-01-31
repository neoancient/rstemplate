package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FILL_DARK_GREY
import org.megamek.rstemplate.templates.FONT_SIZE_VSMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element

const val padding = 3.0
/**
 * Displays a label followed by a square box with rounded corners, optionally containing text.
 */
class DamageCheckBox(private val label: String, private val text: List<String>?,
                     private val boxCount: Int, private val boxHeight: Double?) {

    constructor(label: String, boxCount: Int = 1, boxHeight: Double? = null):
            this(label, null, boxCount, boxHeight)

    constructor(label: String, text: String, boxHeight: Double? = null):
            this(label, listOf(text), 1, boxHeight)

    constructor(label: String, text: List<String>, boxHeight: Double? = null):
            this(label, text, text.size, boxHeight)

    fun draw(sheet: RecordSheet, x: Double = 0.0, y: Double = 0.0, fontSize: Float, width: Double? = null,
             offset: Double? = null, fontWeight: String = SVGConstants.SVG_NORMAL_VALUE, fill: String = FILL_DARK_GREY
    ): Element {
        val g = sheet.createTranslatedGroup(x, y)
        val boxSize = boxHeight ?: sheet.calcFontHeight(fontSize).toDouble()
        val textLength = offset ?: width?.minus((boxSize + padding) * boxCount)
            ?: sheet.calcTextLength("${label}_", fontSize, fontWeight)
        sheet.addTextElement(0.0, boxSize * 0.9, label, fontSize, fontWeight,
            fill, fixedWidth = true, width = textLength, parent = g)
        var xpos = textLength
        for (i in 0 until boxCount) {
            val box = RoundedBorder(xpos, 0.0, boxSize, boxSize, 1.315, 0.726,
                0.96, fill)
            g.appendChild(box.draw(sheet.document))
            if (text != null && i < text.size) {
                sheet.addTextElement(xpos + boxSize * 0.5,
                    (boxSize + sheet.calcFontHeight(FONT_SIZE_VSMALL)) * 0.4,
                    text[i], FONT_SIZE_VSMALL, fill = fill, anchor = SVGConstants.SVG_MIDDLE_VALUE,
                    fixedWidth = true, width = boxSize * 0.6, parent = g)
            }
            xpos += boxSize + padding
        }
        return g
    }
}