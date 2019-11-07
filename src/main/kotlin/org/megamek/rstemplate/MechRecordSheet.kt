package org.megamek.rstemplate

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.CellBorder
import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.layout.RSLabel

/**
 * Base class for Mech record sheets
 */

const val padding = 3.0

open class MechRecordSheet(size: PaperSize) :  RecordSheet(size) {
    val eqTableCell = Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight + padding,
        width() / 3.0, height() / 2.0 - logoHeight - titleHeight - padding)
    val crewCell = eqTableCell.translate(eqTableCell.width, 0.0).scale(1.0, 0.5)
    val fluffImageCell = crewCell.translate(0.0, crewCell.height)
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(), width() / 3.0, height() / 2.0)
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

    fun addRect(cell: Cell) {
        val rect = document.createElementNS(svgNS, SVGConstants.SVG_RECT_TAG)
        rect.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, cell.x.toString())
        rect.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, cell.y.toString())
        rect.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, cell.width.toString())
        rect.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, cell.height.toString())
        rect.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, "none")
        rect.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, "#000000")
        document.documentElement.appendChild(rect)
    }

    fun addEquipmentTable(rect: Cell) {
        addBorder(rect.x, rect.y, rect.width - padding, rect.height - padding,
            "'MECH DATA", true, false)
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