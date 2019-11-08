package org.megamek.rstemplate

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element

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
            "'MECH DATA", true, false, parent = g)
        var ypos = internal.y
        val fontSize = 9.67f
        val lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField("Type:", "type", internal.x, ypos, fontSize, SVGConstants.SVG_BOLDER_VALUE, parent = g)
        ypos += lineHeight
        ypos += addUnitDataFields(internal.x + padding, ypos, internal.width, parent = g)
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, "Weapons & Equipment Inventory:", FONT_SIZE_FREE_LABEL,
            SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, width = internal.width * 0.6, parent = g)
        addTextElement(internal.width * 0.75, ypos, "(hexes)", FONT_SIZE_MEDIUM, fixedWidth = true, parent = g)
        document.documentElement.appendChild(g)
    }

    /**
     * Adds the fields and labels at the top of the unit data panel
     */
    open fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        addTextElement(x, y, "Movement Points:", fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField("Walking:", "mpWalk", "0"),
            LabeledField("Running:", "mpRun", "0"),
            LabeledField("Jumping:", "mpJump", "0")
        ), x, y + lineHeight, fontSize, FILL_DARK_GREY, parent)
        addFieldSet(listOf(
            LabeledField("Tonnage:", "tonnage", "0"),
            LabeledField("Tech Base:", "techBase","Inner Sphere"),
            LabeledField("Rules Level:", "rulesLevel","Standard"),
            LabeledField("Role:", "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, parent)
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
        val label = RSLabel(this, rect.x + rect.width * 0.5, rect.y, "ARMOR DIAGRAM",
            FONT_SIZE_FREE_LABEL, center = true)
        document.documentElement.appendChild(label.draw())
    }

    fun addCritTable(rect: Cell) {
        addBorder(rect.x, rect.y, rect.width - padding, rect.height,
            "CRITICAL TABLE")
    }

    fun addStructureDiagram(rect: Cell) {
        val label = RSLabel(this, rect.x + rect.width * 0.5, rect.y, "INTERNAL STRUCTURE DIAGRAM",
            FONT_SIZE_FREE_LABEL, center = true)
        document.documentElement.appendChild(label.draw())
    }

    fun addHeatPanel(rect: Cell) {
        addBorder(rect.x, rect.y, rect.width - padding, rect.height,
            "HEAT")
    }

    fun addHeatScale(rect: Cell) {

    }
}

class BipedMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
}