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
    val eqTableCell = Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight,
        width() * 0.4, height() / 2.0 - logoHeight - titleHeight)
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(), width() / 3.0, height() / 2.0)
    val crewFluffCell = Cell(eqTableCell.rightX(), eqTableCell.y, width() - eqTableCell.width - armorCell.width, eqTableCell.height)
    val critTableCell = Cell(LEFT_MARGIN.toDouble(), size.height / 2.0, width() * 0.667, height() / 2.0 - footerHeight)
    val heatScaleCell = Cell(armorCell.rightX() - 20, armorCell.bottomY(), 20.0, armorCell.height - footerHeight)
    val structureCell = Cell(armorCell.x, armorCell.bottomY(), armorCell.width - heatScaleCell.width, heatScaleCell.height * 0.5)
    val heatCell = structureCell.translate(0.0, structureCell.height)

    val bundle = ResourceBundle.getBundle(MechRecordSheet::class.java.name)

    init {
        addEquipmentTable(eqTableCell)
        addCrewAndFluffPanels(crewFluffCell)
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
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, parent = parent)
        return lineHeight * 4
    }

    fun addCrewAndFluffPanels(rect: Cell) {
        document.documentElement.appendChild(createPilotPanel(rect, 1, "warriorDataSingle"))
    }

    fun createPilotPanel(rect: Cell, crewSize: Int, id: String): Element {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x},${rect.y})")
        val inside = addBorder(0.0, 0.0, rect.width - padding, 36.0 + crewSize * 51.0,
            bundle.getString("crewPanel.title"), bevelTopRight = false, bevelBottomLeft = false,
            parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = inside.y + lineHeight * 1.5
        for (i in 0 until crewSize) {
            addField(bundle.getString("name"), "pilotName$i", inside.x + padding, ypos, fontSize,
                blankId = "blankCrewName$i",
                blankWidth = inside.width - padding * 2
                        - calcTextLength("${bundle.getString("name")}_", fontSize, fontWeight),
                parent = g)
            ypos += lineHeight * 1.5
            addField(bundle.getString("gunnerySkill"), "gunnerySkill$i", inside.x + padding,
                ypos, fontSize, defaultText = "0",
                fieldOffset = inside.width * 0.35,
                blankId = "blankGunnerySkill$i",
                blankWidth = inside.width * 0.1,
                parent = g)
            addField(bundle.getString("pilotingSkill"), "pilotingSkill$i", inside.x + inside.width * 0.5,
                ypos, fontSize, defaultText = "0",
                fieldOffset = inside.width * 0.35,
                blankId = "blankPilotingSkill$i",
                blankWidth = inside.width - padding - inside.width * 0.85,
                parent = g)
            ypos += lineHeight

            val chartBounds = Cell(inside.width * 0.35, ypos,
                inside.width * 0.65 - 1.5,20.0)
            val path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
            path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                "M ${chartBounds.x},${chartBounds.y + 1.015} c 0,-0.56 0.454,-1.015 1.015,-1.015 l ${chartBounds.width - 2.03},0"
                        + " c 0.561,0 1.016,0.455 1.016,1.015 l 0,${chartBounds.height - 2.03}"
                        + " c 0,0.56 -0.455,1.015 -1.016,1.015 l -${chartBounds.width - 2.03},0"
                        + " c -0.561,0 -1.016,0.455 -1.016,-1.015 l 0,-${chartBounds.height - 2.03} Z")
            path.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
            path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
            path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "1.0")
            path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
            path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
            g.appendChild(path)
            val grid = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
            grid.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                "M ${chartBounds.x},${chartBounds.y + chartBounds.height / 2.0} l ${chartBounds.width},0"
                    + (1..5).map {
                    " M ${chartBounds.x + it * chartBounds.width / 6.0},${chartBounds.y} l 0,${chartBounds.height}"
                }.joinToString(" "))
            grid.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
            grid.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
            grid.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "0.58")
            grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
            grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
            g.appendChild(grid)
            val startx = chartBounds.x - chartBounds.width / 12.0
            val starty = chartBounds.y + lineHeight
            val cons = listOf("3", "5", "7", "10", "11", bundle.getString("dead"))
            for (i in 1..6) {
                addTextElement(g, startx + i * chartBounds.width / 6.0, starty,
                    i.toString(), 5.8f, SVGConstants.SVG_MIDDLE_VALUE, SVGConstants.SVG_BOLD_VALUE,
                    FILL_DARK_GREY)
                addTextElement(g, startx + i * chartBounds.width / 6.0, starty + chartBounds.height / 2.0,
                    cons[i - 1], 5.8f, SVGConstants.SVG_MIDDLE_VALUE, SVGConstants.SVG_BOLD_VALUE,
                    FILL_DARK_GREY)
            }
            addTextElement(g, chartBounds.x - padding, starty, bundle.getString("hitsTaken"),
                5.2f, SVGConstants.SVG_END_VALUE, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY)
            addTextElement(g, chartBounds.x - padding, starty + chartBounds.height / 2.0, bundle.getString("consciousnessNum"),
                5.2f, SVGConstants.SVG_END_VALUE, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY)
        }
        return g
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

    override fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        addField(bundle.getString("tonnage"), "tonnage", x, y, fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, parent = parent)

        addTextElement(x, y + lineHeight, bundle.getString("movementPoints"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addTextElement(x, y + lineHeight * 2, bundle.getString("battlemech"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("walking"), "mpWalk", "0"),
            LabeledField(bundle.getString("running"), "mpRun", "0"),
            LabeledField(bundle.getString("jumping"), "mpJump", "0")
        ), x, y + lineHeight * 3, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)

        addTextElement(x + width * 0.48, y + lineHeight * 3, bundle.getString("airmech"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("walking"), "mpAirMechWalk", "0"),
            LabeledField(bundle.getString("running"), "mpAirMechRun", "0")
        ), x + width * 0.24, y + lineHeight * 4, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("cruising"), "mpAirMechCruise", "0"),
            LabeledField(bundle.getString("flanking"), "mpAirMechFlank", "0")
        ), x + width * 0.48, y + lineHeight * 4, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)

        addTextElement(x + width * 0.72, y + lineHeight * 3, bundle.getString("fighter"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("safeThrust"), "mpSafeThrust", "0"),
            LabeledField(bundle.getString("maxThrust"), "mpMaxThrust", "0")
        ), x + width * 0.72, y + lineHeight * 4, fontSize, FILL_DARK_GREY, 47.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)

        return lineHeight * 6
    }
}

class QuadVeeRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_quadvee.svg"

    override fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        addTextElement(x, y, bundle.getString("movementPoints"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("walking"), "mpWalk", "0"),
            LabeledField(bundle.getString("running"), "mpRun", "0"),
            LabeledField(bundle.getString("jumping"), "mpJump", "0")
        ), x, y + lineHeight, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        addTextElement(x + width * 0.25, y + lineHeight, bundle.getString("vehicle"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("cruising"), "mpCruise", "0"),
            LabeledField(bundle.getString("flanking"), "mpFlank", "0")
        ), x + width * 0.25, y + lineHeight * 2, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, parent = parent)
        return lineHeight * 4
    }
}