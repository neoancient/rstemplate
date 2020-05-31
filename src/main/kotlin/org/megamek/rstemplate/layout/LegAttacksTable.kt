package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_SMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class LegAttacksTable(private val sheet: RecordSheet) {
    private val bundle: ResourceBundle = ResourceBundle.getBundle(LegAttacksTable::class.java.name)

    private val toHitMods = listOf(
        listOf("4-6", "0"),
        listOf("3", "+2"),
        listOf("2", "+5"),
        listOf("1", "+7")
    )

    fun draw(rect: Cell, parent: Element = sheet.rootElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("legAttacks.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / (toHitMods.size + 3)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.3, ypos, bundle.getString("battleArmor"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.75, ypos, bundle.getString("baseToHit"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_SMALL,
            toHitMods, listOf(0.3, 0.75),
            listOf(bundle.getString("troopersActive"),
                bundle.getString("modifier")), firstColAnchor = SVGConstants.SVG_MIDDLE_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)
        parent.appendChild(g)
    }
}