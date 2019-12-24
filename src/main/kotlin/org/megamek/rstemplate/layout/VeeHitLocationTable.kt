package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_VSMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.megamek.rstemplate.templates.truncate
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
abstract class VeeHitLocationTable(private val sheet: RecordSheet) {

    abstract val unitType: String
    abstract val hitLocNotesCount: Int
    abstract val hitLocations: List<List<String>>

    protected val bundle: ResourceBundle = ResourceBundle.getBundle(VeeHitLocationTable::class.java.name)

    fun draw(rect: Cell, parent: Element = sheet.document.documentElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("hitTable.$unitType.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = sheet.calcFontHeight(FONT_SIZE_VSMALL)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.55, ypos, bundle.getString("attackDirection"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            hitLocations, listOf(0.1, 0.3, 0.55, 0.8),
            listOf(bundle.getString("2d6Roll"),
                bundle.getString("dirFront"),
                bundle.getString("dirRear"),
                bundle.getString("dirSides")), parent = g)
        ypos += lineHeight

        val notesG = sheet.document.createElementNS(null, SVGConstants.SVG_G_TAG)
        var notesY = 0.0
        for (i in 1..hitLocNotesCount) {
            notesY += sheet.addParagraph(inner.x + padding, notesY,
                inner.width - padding, bundle.getString("hitLocNotes.$unitType.$i"),
                if (unitType == "naval") 4.8f else FONT_SIZE_VSMALL, notesG)
        }
        notesG.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE}(0,${(inner.bottomY() - notesY - padding * 2 - lineHeight).truncate()})")
        g.appendChild(notesG)
        parent.appendChild(g)
    }
}

open class TankHitLocationTable(sheet: RecordSheet): VeeHitLocationTable(sheet) {
    override val unitType = "tank"
    override val hitLocNotesCount = 4
    override val hitLocations = listOf(
        listOf("2*",
            bundle.getString("front") + bundle.getString("critical"),
            bundle.getString("rear") + bundle.getString("critical"),
            bundle.getString("side") + bundle.getString("critical")),
        listOf("3",
            bundle.getString("front") + "\u2020",
            bundle.getString("rear") + "\u2020",
            bundle.getString("side") + "\u2020"),
        listOf("4",
            bundle.getString("front") + "\u2020",
            bundle.getString("rear") + "\u2020",
            bundle.getString("side") + "\u2020"),
        listOf("5",
            bundle.getString("rightSide") + "\u2020",
            bundle.getString("leftSide") + "\u2020",
            bundle.getString("front") + "\u2020"),
        listOf("6",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("7",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("8",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side") + bundle.getString("critical") + "*"),
        listOf("9",
            bundle.getString("leftSide") + "\u2020",
            bundle.getString("rightSide") + "\u2020",
            bundle.getString("rear") + "\u2020"),
        listOf("10",
            bundle.getString("turret"),
            bundle.getString("turret"),
            bundle.getString("turret")),
        listOf("11",
            bundle.getString("turret"),
            bundle.getString("turret"),
            bundle.getString("turret")),
        listOf("12*",
            bundle.getString("turret") + bundle.getString("critical"),
            bundle.getString("turret") + bundle.getString("critical"),
            bundle.getString("turret") + bundle.getString("critical"))
    )
}

class NavalHitLocationTable(sheet: RecordSheet): TankHitLocationTable(sheet) {
    override val unitType = "naval"
}

class VTOLHitLocationTable(sheet: RecordSheet): VeeHitLocationTable(sheet) {
    override val unitType = "vtol"
    override val hitLocNotesCount = 3
    override val hitLocations = listOf(
        listOf("2*",
            bundle.getString("front") + bundle.getString("critical"),
            bundle.getString("rear") + bundle.getString("critical"),
            bundle.getString("side") + bundle.getString("critical")),
        listOf("3",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020"),
        listOf("4",
            bundle.getString("turret") + "\u2021",
            bundle.getString("turret") + "\u2021",
            bundle.getString("turret") + "\u2021"),
        listOf("5",
            bundle.getString("rightSide") + "\u2020",
            bundle.getString("leftSide") + "\u2020",
            bundle.getString("front") + "\u2020"),
        listOf("6",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("7",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("8",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side") + bundle.getString("critical") + "*"),
        listOf("9",
            bundle.getString("leftSide"),
            bundle.getString("rightSide"),
            bundle.getString("rear")),
        listOf("10",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020"),
        listOf("11",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020"),
        listOf("12*",
            bundle.getString("rotors") + bundle.getString("critical") + "*\u2020",
            bundle.getString("rotors") + bundle.getString("critical") + "*\u2020",
            bundle.getString("rotors") + bundle.getString("critical") + "*\u2020"))
}