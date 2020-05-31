package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_SMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class SwarmAttackLocTable(private val sheet: RecordSheet) {
    private val bundle: ResourceBundle = ResourceBundle.getBundle(SwarmAttackLocTable::class.java.name)

    private val hitLocations = listOf(
        listOf("2", bundle.getString("head"), bundle.getString("head")),
        listOf("3", bundle.getString("ctr"), bundle.getString("rt")),
        listOf("4", bundle.getString("rtr"), bundle.getString("ctr")),
        listOf("5", bundle.getString("rt"), bundle.getString("rtr")),
        listOf("6", bundle.getString("ra"), bundle.getString("rt")),
        listOf("7", bundle.getString("ct"), bundle.getString("ct")),
        listOf("8", bundle.getString("la"), bundle.getString("lt")),
        listOf("9", bundle.getString("lt"), bundle.getString("ltr")),
        listOf("10", bundle.getString("ltr"), bundle.getString("ctr")),
        listOf("11", bundle.getString("ctr"), bundle.getString("lt")),
        listOf("12", bundle.getString("head"), bundle.getString("head"))
    )

    fun draw(rect: Cell, parent: Element = sheet.rootElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("swarmAttackMods.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / (hitLocations.size + 3)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.08, ypos, bundle.getString("2d6"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.4, ypos, bundle.getString("bipedal"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.8, ypos, bundle.getString("quad"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_SMALL,
            hitLocations, listOf(0.08, 0.4, 0.8),
            listOf(bundle.getString("roll"), bundle.getString("location"), bundle.getString("location")),
            firstColAnchor = SVGConstants.SVG_MIDDLE_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)
        parent.appendChild(g)
    }
}