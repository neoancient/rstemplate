package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_SMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class SwarmAttackModsTable(private val sheet: RecordSheet) {

    private val bundle: ResourceBundle = ResourceBundle.getBundle(SwarmAttackModsTable::class.java.name)

    private val friendlyMechanized = listOf(
        listOf("6", "+0", "+0", "+0", "+0", "+1", "+2"),
        listOf("5", "+0", "+0", "+0", "+1", "+2", "+3"),
        listOf("4", "+0", "+0", "+1", "+2", "+3", "+4"),
        listOf("3", "+0", "+1", "+2", "+3", "+4", "+5"),
        listOf("2", "+1", "+2", "+3", "+4", "+5", "+6"),
        listOf("1", "+2", "+3", "+4", "+5", "+6", "+7")
    )
    private val situation = listOf(
        listOf(bundle.getString("mechProne"), "-2"),
        listOf(bundle.getString("immobile"), "-4"),
        listOf(bundle.getString("vehicle"), "-2")
    )

    fun draw(rect: Cell, parent: Element = sheet.rootElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("swarmAttackMods.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / (friendlyMechanized.size + situation.size + 10)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.2, ypos, bundle.getString("enemy.1"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.69, ypos, bundle.getString("friendly.1"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        sheet.addTextElement(inner.width * 0.2, ypos, bundle.getString("enemy.2"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.69, ypos, bundle.getString("friendly.2"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_SMALL,
            friendlyMechanized, listOf(0.2, 0.44, 0.54, 0.64, 0.74, 0.84, 0.94),
            listOf(bundle.getString("troopersActive"), "1", "2", "3", "4", "5", "6"),
            firstColAnchor = SVGConstants.SVG_MIDDLE_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)
        ypos += lineHeight

        sheet.addTextElement(inner.width * 0.04, ypos, bundle.getString("battleArmorEquipment"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += lineHeight
        sheet.addTextElement(inner.width * 0.04, ypos, bundle.getString("clawsWithMagnets"),
            FONT_SIZE_SMALL, SVGConstants.SVG_NORMAL_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.74, ypos, "-1",
            FONT_SIZE_SMALL, SVGConstants.SVG_NORMAL_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight * 2

        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_SMALL,
            situation, listOf(0.04, 0.74),
            listOf(bundle.getString("situation"), ""), firstColAnchor = SVGConstants.SVG_START_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)

        sheet.addTextElement(inner.width * 0.04, ypos, bundle.getString("cumulativeNote"),
            FONT_SIZE_SMALL, SVGConstants.SVG_NORMAL_VALUE, parent = g)
        parent.appendChild(g)
    }
}
