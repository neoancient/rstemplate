package org.megamek.rstemplate

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 * Base class for Mech record sheets
 */

const val padding = 3.0

abstract class MechRecordSheet(size: PaperSize) :  RecordSheet(size) {
    val eqTableCell = Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight + padding,
        width() * 0.4, height() / 2.0 - logoHeight - titleHeight - padding)
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(), width() / 3.0, height() / 2.0)
    val crewCell = Cell(eqTableCell.rightX(), eqTableCell.y, width() - eqTableCell.width - armorCell.width, eqTableCell.height / 2.0)
    val fluffImageCell = crewCell.translate(0.0, crewCell.height)
    val critTableCell = Cell(LEFT_MARGIN.toDouble(), size.height / 2.0, width() * 0.667, height() / 2.0 - footerHeight)
    val heatScaleCell = Cell(armorCell.rightX() - 20, armorCell.bottomY(), 20.0, armorCell.height - footerHeight)
    val structureCell = Cell(armorCell.x, armorCell.bottomY(), armorCell.width - heatScaleCell.width, heatScaleCell.height * 0.5)
    val heatCell = structureCell.translate(0.0, structureCell.height)

    val bundle = ResourceBundle.getBundle(MechRecordSheet::class.java.name)

    init {
        addEquipmentTable(eqTableCell)
        addCrewPanel(crewCell)
        addFluffImageArea(fluffImageCell)
        addArmorDiagram(armorCell)
        addCritTable(critTableCell)
        addStructureDiagram(structureCell)
        addHeatPanel(heatCell)
        addHeatScale(heatScaleCell)
    }

    fun addEquipmentTable(rect: Cell) {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x},${rect.y})")
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unitDataPanel")
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            bundle.getString("dataPanel.title"), true, false, parent = g)
        var ypos = internal.y
        val fontSize = 9.67f
        val lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x, ypos, fontSize, SVGConstants.SVG_BOLDER_VALUE, parent = g)
        ypos += lineHeight
        ypos += addUnitDataFields(internal.x + padding, ypos, internal.width, parent = g)
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, bundle.getString("weaponsAndEquipment"), FONT_SIZE_FREE_LABEL,
            SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, width = internal.width * 0.6, parent = g)
        addTextElement(internal.width * 0.75, ypos, bundle.getString("hexes"), FONT_SIZE_MEDIUM, fixedWidth = true, parent = g)

        addRect(internal.x, ypos + padding, internal.width - padding, internal.bottomY() - ypos - padding - lineHeight * 2.0,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g)

        addHorizontalLine(internal.x, internal.bottomY() - padding - lineHeight * 1.5, internal.width - padding, parent = g)
        addField(bundle.getString("bv"), "bv", internal.x + padding + bevelX, internal.bottomY() - padding * 2,
            fontSize, defaultText = "0", parent = g)
        addRect(rect.width * 0.5 + (rect.width * 0.5 - tabBevelX - padding) * 0.5 - 10,
            internal.bottomY() - padding * 0.5 - lineHeight * 0.75 + tabBevelY * 0.5 - 10.0,
            20.0, 20.0, id = "eraIcon", parent = g)
        document.documentElement.appendChild(g)
    }

    /**
     * Adds the fields and labels at the top of the unit data panel
     */
    open fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        addTextElement(x, y, bundle.getString("movementPoints"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("walking"), "mpWalk", "0"),
            LabeledField(bundle.getString("running"), "mpRun", "0"),
            LabeledField(bundle.getString("jumping"), "mpJump", "0")
        ), x, y + lineHeight, fontSize, FILL_DARK_GREY, 50.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, parent = parent)
        return lineHeight * 4
    }

    fun addCrewPanel(rect: Cell) {
        addBorder(rect.x, rect.y, rect.width - padding, rect.height - padding,
            "WARRIOR DATA", bevelTopRight = false, bevelBottomLeft = false)
    }
    fun addFluffImageArea(rect: Cell) {
        //addRect(rect)
    }
    fun addArmorDiagram(rect: Cell) {
        val label = RSLabel(this, rect.x + rect.width * 0.5, rect.y, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        document.documentElement.appendChild(label.draw())
    }

    fun addCritTable(rect: Cell) {
        addBorder(rect.x, rect.y, rect.width - padding, rect.height,
            bundle.getString("critTablePanel.title"))
    }

    fun addStructureDiagram(rect: Cell) {
        val label = RSLabel(this, rect.x + rect.width * 0.5, rect.y, bundle.getString("isPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        document.documentElement.appendChild(label.draw())
    }

    fun addHeatPanel(rect: Cell) {
        addBorder(rect.x, rect.y, rect.width - padding, rect.height,
            bundle.getString("heatPanel.title"))
    }

    fun addHeatScale(rect: Cell) {

    }
}

class BipedMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_biped_default.svg"
}

class QuadMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_quad_default.svg"
}

class TripodMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_tripod_default.svg"
}

class LAMRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_biped_lam.svg"
}

class QuadVeeRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_quadvee.svg"
}