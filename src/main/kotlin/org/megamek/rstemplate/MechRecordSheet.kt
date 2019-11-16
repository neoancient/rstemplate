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

    protected val bundle = ResourceBundle.getBundle(MechRecordSheet::class.java.name)

    init {
        addEquipmentTable(eqTableCell)
        addCrewAndFluffPanels(crewFluffCell)
        addArmorDiagram(armorCell)
        addCritTable(critTableCell)
        addStructureDiagram(structureCell)
        addHeatPanel(heatCell)
        addHeatScale(heatScaleCell)
    }

    open fun isQuad() = false
    open fun isTripod() = false

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

    open fun addCrewAndFluffPanels(rect: Cell) {
        val tempG = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        val tempBorder = addBorder(rect.x, rect.y, rect.width, rect.height,
            bundle.getString("crewPanel.title"), bevelTopRight = false, bevelBottomLeft = false, parent = tempG)
        val contentGroup = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        contentGroup.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${tempBorder.x},${tempBorder.y})")
        var ypos = 0.0
        for (i in 0 until maxCrew()) {
            ypos = addCrewData(i, ypos, tempBorder.width, contentGroup)
            val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
            g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
                "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x},${rect.y})")
            g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "warriorData${crewSizeId[i]}")
            ypos += addCrewDamageTrack(0.0, ypos, tempBorder.width,
                id = "crewDamage$i", hidden = hideCrewIndex(i), parent = contentGroup)
            addRect(0.0, ypos, tempBorder.width, 13.5, id = "spas$i", parent = contentGroup)
            addBorder(0.0, 0.0, rect.width, tempBorder.y - rect.y + ypos + padding * 6,
                bundle.getString("crewPanel.title"), bevelTopRight = false, bevelBottomLeft = false,
                parent = g)
            addRect(0.0, tempBorder.y - rect.y + ypos + padding * 6, rect.width - padding * 2,
                rect.height - tempBorder.y + rect.y - ypos - padding * 6,
                id = "fluff${crewSizeId[i]}Pilot", parent = g)
            if (hideCrewIndex(i)) {
                g.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE)
            }
            document.documentElement.appendChild(g)
        }
        document.documentElement.appendChild(contentGroup)
    }

    fun addCrewDamageTrack(x: Double, y: Double, width: Double, height: Double = 20.0,
                           id: String? = null, hidden: Boolean = false, parent: Element): Double {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        if (id != null) {
            g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        }
        val chartBounds = Cell(width * 0.35, y,
            width * 0.65 - padding,20.0)
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
        val starty = chartBounds.y + calcFontHeight(5.8f)
        val cons = listOf("3", "5", "7", "10", "11", bundle.getString("dead"))
        for (i in 1..6) {
            addTextElement(g, startx + i * chartBounds.width / 6.0, starty,
                i.toString(), 5.8f, SVGConstants.SVG_MIDDLE_VALUE, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY)
            addTextElement(g, startx + i * chartBounds.width / 6.0, starty + chartBounds.height / 2.0,
                cons[i - 1], 5.8f, SVGConstants.SVG_MIDDLE_VALUE, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY, maxWidth = chartBounds.width / 6.0 - 4.0)
        }
        addTextElement(g, chartBounds.x - padding, starty, bundle.getString("hitsTaken"),
            5.2f, SVGConstants.SVG_END_VALUE, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY)
        addTextElement(g, chartBounds.x - padding, starty + chartBounds.height / 2.0, bundle.getString("consciousnessNum"),
            5.2f, SVGConstants.SVG_END_VALUE, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY)
        parent.appendChild(g)
        return height
    }

    open fun addCrewData(crewIndex: Int, y: Double, width: Double, parent: Element): Double {
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = y + lineHeight * 1.5
        addField(bundle.getString("name"), "pilotName$crewIndex", padding, ypos, fontSize,
            blankId = "blankCrewName$crewIndex",
            blankWidth = width - padding * 2
                    - calcTextLength("${bundle.getString("name")}_", fontSize, fontWeight),
            labelId = "crewName$crewIndex", labelFixedWidth = false, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        ypos += lineHeight * 1.5
        addField(bundle.getString("gunnerySkill"), "gunnerySkill$crewIndex", padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "blankGunnerySkill$crewIndex", labelId = "gunnerySkillText$crewIndex",
            blankWidth = width * 0.13, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        addField(bundle.getString("pilotingSkill"), "pilotingSkill$crewIndex", width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "blankPilotingSkill$crewIndex", labelId = "pilotingSkillText$crewIndex",
            blankWidth = width * 0.18 - padding, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        ypos += lineHeight
        return ypos
    }

    open fun maxCrew() = 2

    open fun hideCrewIndex(i: Int) = i > 0

    fun addArmorDiagram(rect: Cell) {
        val label = RSLabel(this, rect.x + rect.width * 0.5, rect.y, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        document.documentElement.appendChild(label.draw())
    }

    fun addCritTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("critTablePanel.title"), parent = g)
        val colWidth = internal.width / 3.0 - padding * 4.0
        val fontSize = 9.65f
        var ypos = internal.y + internal.height * 0.05
        if (isQuad()) {
            addSingleCritLocation(internal.x + padding, ypos + internal.height * 0.087, colWidth, internal.height * 0.145,"crits_FLL", fontSize, g)
            addSingleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos + internal.height * 0.087, colWidth, internal.height * 0.145,"crits_FRL", fontSize, g)
        } else {
            addDoubleCritLocation(internal.x + padding, ypos, colWidth, internal.height * 0.3,"crits_LA", fontSize, g)
            addDoubleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos, colWidth, internal.height * 0.3,"crits_RA", fontSize, g)
        }
        ypos += internal.height * 0.387
        addDoubleCritLocation(internal.x + padding, ypos, colWidth, internal.height * 0.3,"crits_LT", fontSize, g)
        addDoubleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos, colWidth, internal.height * 0.3, "crits_RT", fontSize, g)
        ypos += internal.height * 0.387
        addSingleCritLocation(internal.x + padding, ypos, colWidth, internal.height * 0.145,
            if (isQuad()) "crits_RLL" else "crits_LL", fontSize, g)
        addSingleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos, colWidth, internal.height * 0.145,
            if (isQuad()) "crits_RRL" else "crits_RL", fontSize, g)
        if (isTripod()) {
            addSingleCritLocation(internal.x + colWidth + padding * 2.0, ypos, colWidth, internal.height * 0.145,
                "crits_CL", fontSize, g)
        }
        ypos = internal.y + internal.height * 0.02
        addSingleCritLocation(internal.x + colWidth + padding * 2.0, ypos, colWidth, internal.height * 0.145, "crits_HD", fontSize, g)
        ypos += internal.height * 0.215
        addDoubleCritLocation(internal.x + colWidth + padding * 2.0, ypos, colWidth, internal.height * 0.3, "crits_CT", fontSize, g)
        document.documentElement.appendChild(g)
    }

    fun addDoubleCritLocation(x: Double, y: Double, width: Double, height: Double, id: String,
                              fontSize: Float, parent: Element) {
        val lineHeight = calcFontHeight(fontSize)
        addTextElement(x, y + (height - 5) * 0.25 + lineHeight * 0.5, "1-3", fontSize, SVGConstants.SVG_BOLD_VALUE,
            parent = parent)
        addTextElement(x, y + (height - 5) * 0.75 + 5.0 + lineHeight * 0.5, "4-6", fontSize, SVGConstants.SVG_BOLD_VALUE,
            parent = parent)
        addRect(x + 18.0, y, width - 18.0, height, id = id, parent = parent)
    }

    fun addSingleCritLocation(x: Double, y: Double, width: Double, height: Double, id: String,
                              fontSize: Float, parent: Element) {
        addRect(x + 18.0, y, width - 18.0, height, id = id, parent = parent)
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
    override fun isQuad() = true
}

class TripodMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_tripod_default.svg"

    override fun isTripod() = true

    override fun maxCrew() = 3

    override fun hideCrewIndex(i: Int) = i > 1
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

    override fun maxCrew() = 1

    override fun hideCrewIndex(i: Int) = false

    override fun addCrewAndFluffPanels(rect: Cell) {
        super.addCrewAndFluffPanels(Cell(rect.x, rect.y, rect.width, rect.height - 70.0 - padding))
        addAeroMovementCompass(Cell(rect.x, rect.y + rect.height - 70.0, rect.width - padding * 2.0, 70.0))
    }

    override fun addCrewData(crewIndex: Int, y: Double, width: Double, parent: Element): Double {
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = y + lineHeight * 1.5
        addField(bundle.getString("name"), "pilotName$crewIndex", padding, ypos, fontSize,
            blankId = "blankCrewName$crewIndex",
            blankWidth = width - padding * 2
                    - calcTextLength("${bundle.getString("name")}_", fontSize, fontWeight),
            labelId = "crewName$crewIndex", labelFixedWidth = false, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        ypos += lineHeight * 1.5
        addTextElement(parent, padding, ypos, bundle.getString("battlemech"), fontSize, weight = fontWeight)
        ypos += lineHeight
        addField(bundle.getString("gunnerySkill"), "gunnerySkill$crewIndex", padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "blankGunnerySkill$crewIndex", labelId = "gunnerySkillText$crewIndex",
            blankWidth = width * 0.13, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        addField(bundle.getString("pilotingSkill"), "pilotingSkill$crewIndex", width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "blankPilotingSkill$crewIndex", labelId = "pilotingSkillText$crewIndex",
            blankWidth = width * 0.18 - padding, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        ypos += lineHeight
        addTextElement(parent, padding, ypos, bundle.getString("aerospace"), fontSize, weight = fontWeight)
        ypos += lineHeight
        addField(bundle.getString("gunnerySkill"), "asfGunnerySkill", padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "asfBlankGunnerySkill", labelId = "asfGunnerySkillText",
            blankWidth = width * 0.13, parent = parent)
        addField(bundle.getString("pilotingSkill"), "asfPilotingSkill", width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "asfBlankPilotingSkill", labelId = "asfPilotingSkillText",
            blankWidth = width * 0.18 - padding, parent = parent)
        ypos += lineHeight
        return ypos
    }
}

class QuadVeeRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_quadvee.svg"

    override fun isQuad() = true

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

    override fun maxCrew() = 2

    override fun hideCrewIndex(i: Int) = false
}