package org.megamek.rstemplate

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import java.util.*

/**
 *
 */
abstract class VehicleRecordSheet(size: PaperSize): RecordSheet(size) {
    val eqTableCell = Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight,
        width() * 0.40, (height() - footerHeight) / 2.0 - logoHeight - titleHeight)
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(), width() / 3.0,
        (height() - footerHeight) / 2.0 - padding)
    val crewCell = Cell(eqTableCell.rightX(), eqTableCell.y, width() - eqTableCell.width - armorCell.width, eqTableCell.height / 3.0)
    val criticalDamageCell = Cell(crewCell.x, crewCell.bottomY(), crewCell.width, crewCell.height)
    val notesCell = Cell(crewCell.x, criticalDamageCell.bottomY(), crewCell.width, crewCell.height)

    protected val bundle = ResourceBundle.getBundle(VehicleRecordSheet::class.java.name)
    open val halfPage = true

    override fun build() {
        addEquipmentTable(eqTableCell)
        addCrewPanel(crewCell)
        addCriticalDamagePanel(criticalDamageCell)
        addNotesPanel(notesCell)
        addArmorDiagram(armorCell)
    }

    fun addEquipmentTable(rect: Cell) {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x.truncate()},${rect.y.truncate()})")
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unitDataPanel")
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            bundle.getString("dataPanel.title"), true, false, parent = g)
        var ypos = internal.y
        var fontSize = 9.67f
        var lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x, ypos, fontSize, SVGConstants.SVG_BOLDER_VALUE, parent = g)
        ypos += lineHeight
        fontSize = 7.7f
        lineHeight = calcFontHeight(fontSize)
        addTextElement(internal.x + padding, ypos, bundle.getString("movementPoints"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = g)
        addFieldSet(listOf(
            LabeledField(bundle.getString("cruising"), "mpWalk", "0"),
            LabeledField(bundle.getString("flanking"), "mpRun", "0"),
            LabeledField(bundle.getString("jumping"), "mpJump", "0", labelId="lblJump")
        ), internal.x + padding, ypos + lineHeight, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), internal.x + padding + internal.width * 0.5, ypos, fontSize, FILL_DARK_GREY, parent = g)
        ypos += lineHeight * 4
        addFieldSet(listOf(
            LabeledField(bundle.getString("movementType"), "movementType"),
            LabeledField(bundle.getString("engineType"), "engineType")
        ), internal.x + padding, ypos, fontSize, FILL_DARK_GREY, parent = g)
        ypos += lineHeight * 2
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, bundle.getString("weaponsAndEquipment"), FONT_SIZE_FREE_LABEL,
            SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, width = internal.width * 0.6, parent = g)
        addTextElement(internal.width * 0.78, ypos, bundle.getString("hexes"), FONT_SIZE_MEDIUM, fixedWidth = true, parent = g)

        addRect(internal.x, ypos, internal.width - padding, internal.bottomY() - ypos - lineHeight * 2.0,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g)

        addHorizontalLine(internal.x, internal.bottomY() - padding - lineHeight * 1.5, internal.width - padding, parent = g)
        addField(bundle.getString("bv"), "bv", internal.x + padding + bevelX, internal.bottomY() - padding * 2,
            fontSize, defaultText = "0", parent = g)
        addRect(rect.width * 0.5 + (rect.width * 0.5 - tabBevelX - padding) * 0.5 - 10,
            internal.bottomY() - padding * 0.5 - lineHeight * 0.75 + tabBevelY * 0.5 - 10.0,
            20.0, 20.0, id = "eraIcon", parent = g)
        document.documentElement.appendChild(g)
    }

    fun addCrewPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("crewPanel.title"), parent = g)
        document.documentElement.appendChild(g)
    }

    fun addCriticalDamagePanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("criticalDamage.title"), parent = g)
        document.documentElement.appendChild(g)
    }

    fun addNotesPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("notes.title"), parent = g)
        document.documentElement.appendChild(g)
    }

    fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(this, rect.width * 0.5, 0.0, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        embedImage(rect.width - 50.0, rect.height - 30.0, 50.0, 30.0,
            CGL_LOGO, anchor = ImageAnchor.BOTTOM_RIGHT, parent = g)
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
    }
}

class WheeledVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_wheeled_default"
}