package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_VSMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
abstract class VeeMotiveDamageTable(private val sheet: RecordSheet) {

    abstract val unitType: String
    abstract val narrow: Boolean
    abstract val motiveTypeMods: List<List<String>>

    protected val bundle = ResourceBundle.getBundle(VeeMotiveDamageTable::class.java.name)

    fun draw(rect: Cell, parent: Element = sheet.document.documentElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("motiveSystemTable.$unitType.title"), false, false,
            parent = g)
        val lineHeight = sheet.calcFontHeight(FONT_SIZE_VSMALL)
        var ypos = inner.y + lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            listOf(
                listOf("2-5", bundle.getString("noEffect")),
                listOf("6-7", bundle.getString("motiveMinorDamage.$unitType")),
                listOf("8-9", bundle.getString("motiveModerateDamage.$unitType")),
                listOf("10-11", bundle.getString("motiveHeavyDamage.$unitType")),
                listOf("12", bundle.getString("motiveMajorDamage.$unitType"))
            ), listOf(0.15, 0.27), listOf(bundle.getString("2d6Roll"), bundle.getString("effect")),
            SVGConstants.SVG_START_VALUE, true, SVGConstants.SVG_MIDDLE_VALUE, g)
        val tableHeight = sheet.createTable(0.0, ypos + lineHeight, inner.width * 0.5, FONT_SIZE_VSMALL,
            listOf(
                listOf(bundle.getString("hitFromRear"), "+1"),
                listOf(bundle.getString("hitFromSide"), "+2")
            ), listOf(0.1, 0.75), listOf(bundle.getString("attackDirectionModifier")),
            SVGConstants.SVG_START_VALUE, false, parent = g)
        if (narrow) {
            ypos += tableHeight
        }
        ypos += sheet.createTable(if (narrow) 0.0 else inner.width * 0.5, ypos + lineHeight, inner.width * 0.5,
            FONT_SIZE_VSMALL,
            motiveTypeMods, listOf(0.1, 0.75), listOf(bundle.getString("vehicleTypeModifier")),
            SVGConstants.SVG_START_VALUE, false, parent = g)
        ypos += lineHeight * 0.5
        sheet.addParagraph(inner.x + padding, ypos,
            inner.width - padding,
            bundle.getString("motiveSystemNote.$unitType"),
            if (narrow) 3.8f else 4.8f, g)
        parent.appendChild(g)
    }
}

class TankMotiveDamageTable(sheet: RecordSheet): VeeMotiveDamageTable(sheet) {
    override val unitType = "tank"
    override val narrow = false
    override val motiveTypeMods = listOf(
        listOf(bundle.getString("trackedNaval"), "+0"),
        listOf(bundle.getString("wheeled"), "+2"),
        listOf(bundle.getString("hovercraftHydrofoil"), "+3"),
        listOf(bundle.getString("wige"), "+4"))
}

class NavalMotiveDamageTable(sheet: RecordSheet): VeeMotiveDamageTable(sheet) {
    override val unitType = "naval"
    override val narrow = true
    override val motiveTypeMods = listOf(
        listOf(bundle.getString("naval"), "+0"),
        listOf(bundle.getString("hydrofoil"), "+3"))
}