package org.megamek.rstemplate

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.PaperSize

/**
 * Base class for Mech record sheets
 */
open class MechRecordSheet(size: PaperSize) :  RecordSheet(size) {
    val eqTableCell = Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight, width() / 3.0, height() / 2.0 - logoHeight - titleHeight)
    val crewCell = eqTableCell.translate(eqTableCell.width, 0.0).scale(1.0, 0.5)
    val fluffImageCell = crewCell.translate(0.0, crewCell.height)
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(), width() / 3.0, height() / 2.0)
    val critTableCell = Cell(LEFT_MARGIN.toDouble(), size.height / 2.0, width() * 0.667, height() / 2.0 - footerHeight)
    val structureHeatCell = Cell(armorCell.x, armorCell.bottomY(), armorCell.width, armorCell.height - footerHeight)

    init {
        addEquipmentTable(eqTableCell)
        addCrewPanel(crewCell)
        addFluffImageArea(fluffImageCell)
        addArmorDiagram(armorCell)
        addCritTable(critTableCell)
        addStructureHeat(structureHeatCell)
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
        addRect(rect)
    }
    fun addCrewPanel(rect: Cell) {
        addRect(rect)
    }
    fun addFluffImageArea(rect: Cell) {
        addRect(rect)
    }
    fun addArmorDiagram(rect: Cell) {
        addRect(rect)
    }
    fun addCritTable(rect: Cell) {
        addRect(rect)
    }
    fun addStructureHeat(rect: Cell) {
        addRect(rect)
    }

}