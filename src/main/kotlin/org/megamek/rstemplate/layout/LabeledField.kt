package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.FILL_BLACK
import org.megamek.rstemplate.FILL_DARK_GREY
import org.megamek.rstemplate.RecordSheet
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Creates a labeled field, assigning an id to the field element. Optionally the field can be replaced
 * by a blank line. The field is placed after the label by default, but can be given an absolute position
 * when lining it up with other fields in a column.
 *
 * @param labelText   The text for the label element
 * @param id          The id to assign to the field element
 * @param defaultText The text for the field element
 * @param labelId     If non-null, the id that will be assigned to the label element
 * @param blankId     The id to assign the blank line, if any
 */
class LabeledField(val labelText: String, val id: String,
                   val defaultText: String = "Lorem Ipsum",
                   val labelId: String? = null, val blankId: String? = null) {

    /**
     * Adds the label, field, and blank line (if any) elements to the document.
     *
     * @param sheet       The record sheet instance
     * @param x           The x coordinate of the start of the label
     * @param y           The y coordinate of the elements
     * @param fontSize    The size of font used for the text
     * @param fontWeight  The weight of the font to use
     * @param fill        The color of the text and blank line
     * @param fieldX      The x coordinate of the start of the field element. If null, it will be placed
     *                    after the label with an underscore-width space between them.
     * @param lineWidth   The width of the blank line. If null, no blank line is drawn.
     * @param fieldAnchor Sets justification of the field element. If this is middle or end, the fieldX
     *                    parameter should be provided a value that will ensure enough space that the
     *                    field text does not overlap the label.
     * @param parent      The parent for the label and field elements
     */
    fun draw(sheet: RecordSheet, x: Double, y: Double, fontSize: Float,
             fill: String = FILL_DARK_GREY, fieldX: Double? = null, lineWidth: Double? = null,
             fieldAnchor: String = SVGConstants.SVG_START_VALUE, labelFixedWidth: Boolean = true,
             hidden: Boolean = false, parent: Element) {
        sheet.addTextElement(x, y, labelText, fontSize, SVGConstants.SVG_BOLD_VALUE, fill,
            SVGConstants.SVG_START_VALUE, id = labelId, fixedWidth = labelFixedWidth, hidden = hidden, parent = parent)
        val xpos = fieldX ?: x + sheet.calcTextLength("${labelText}_", fontSize, SVGConstants.SVG_BOLD_VALUE)
        sheet.addTextElement(xpos, y, defaultText, fontSize, SVGConstants.SVG_NORMAL_VALUE, fill,
            fieldAnchor, id, hidden = hidden, parent = parent)
        if (lineWidth != null) {
            sheet.addHorizontalLine(xpos, y + 1.0, lineWidth, 0.72, fill,
                id = blankId ?: "blank$id", hidden = hidden, parent = parent)
        }
    }
}