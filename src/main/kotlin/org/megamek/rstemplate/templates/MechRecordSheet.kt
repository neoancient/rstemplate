package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.lang.String.format
import java.lang.String.join
import java.util.*

/**
 * Base class for Mech record sheets
 */

abstract class MechRecordSheet(size: PaperSize) :  RecordSheet(size) {
    val eqTableCell = Cell(
        LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight,
        width() * 0.4, (height() - footerHeight) / 2.0 - logoHeight - titleHeight)
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(), width() / 3.0,
        (height() - footerHeight) / 2.0 - padding
    )
    val crewFluffCell = Cell(eqTableCell.rightX(), eqTableCell.y, width() - eqTableCell.width - armorCell.width, eqTableCell.height)
    val critTableCell = Cell(LEFT_MARGIN.toDouble(), armorCell.bottomY() + padding, width() * 0.667, (height() - footerHeight) / 2.0 - padding)
    val heatScaleCell = Cell(armorCell.rightX() - 20, armorCell.bottomY(), 20.0, (height() - footerHeight) / 2.0)
    val structureCell = Cell(armorCell.x, armorCell.bottomY(), armorCell.width - heatScaleCell.width, heatScaleCell.height * 0.5)
    val heatCell = structureCell.translate(0.0, structureCell.height)

    protected val bundle = ResourceBundle.getBundle(MechRecordSheet::class.java.name)

    abstract val damageTransferFileName: String
    abstract val armorDiagramFileName: String
    abstract val isDiagramFileName: String

    override final fun height() = super.height()

    open fun isQuad() = false
    open fun isTripod() = false
    open val systems = listOf(Pair(bundle.getString("engineHits"), 3),
        Pair(bundle.getString("gyroHits"), 3),
        Pair(bundle.getString("sensorHits"), 2),
        Pair(bundle.getString("lifeSupport"), 1))

    override fun build() {
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
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x.truncate()},${rect.y.truncate()})")
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unitDataPanel")
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            bundle.getString("dataPanel.title"), false,true,
            false, parent = g)
        var ypos = internal.y
        val fontSize = 9.67f
        val lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x, ypos, fontSize, SVGConstants.SVG_BOLDER_VALUE, parent = g)
        ypos += lineHeight
        ypos += addUnitDataFields(internal.x + padding, ypos, internal.width, parent = g)
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, bundle.getString("weaponsAndEquipment"),
            FONT_SIZE_FREE_LABEL,
            SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, width = internal.width * 0.6, parent = g)
        addTextElement(internal.width * 0.78, ypos, bundle.getString("hexes"),
            FONT_SIZE_MEDIUM, fixedWidth = true, parent = g)

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
        val tempBorder = addBorder(rect.x, rect.y, rect.width - padding, rect.height,
            bundle.getString("crewPanel.title"), bevelTopRight = false, bevelBottomLeft = false, parent = tempG)
        val contentGroup = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        contentGroup.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${tempBorder.x.truncate()},${tempBorder.y.truncate()})")
        var ypos = 0.0
        for (i in 0 until maxCrew()) {
            ypos = addCrewData(i, ypos, tempBorder.width, contentGroup)
            val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
            g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
                "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x.truncate()},${rect.y.truncate()})")
            g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "warriorData${crewSizeId[i]}")
            ypos += addCrewDamageTrack(0.0, ypos, tempBorder.width,
                id = "crewDamage$i", parent = contentGroup)
            addRect(0.0, ypos, tempBorder.width, 13.5, id = "spas$i", parent = contentGroup)
            addBorder(0.0, 0.0, rect.width - padding, tempBorder.y - rect.y + ypos + padding * 6,
                bundle.getString("crewPanel.title"), bevelTopRight = false, bevelBottomLeft = false,
                parent = g)
            addRect(0.0, tempBorder.y - rect.y + ypos + padding * 6, rect.width - padding * 3,
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
                           id: String? = null, parent: Element): Double {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        if (id != null) {
            g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        }
        val chartBounds = Cell(x + width * 0.35, y,
            width * 0.65 - padding,20.0)
        val outline = RoundedBorder(chartBounds.x, chartBounds.y, chartBounds.width, chartBounds.height,
            1.015, 0.56, 1.0).draw(document)
        g.appendChild(outline)
        val grid = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        grid.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${chartBounds.x.truncate()},${(chartBounds.y + chartBounds.height / 2.0).truncate()}"
                    + " l ${chartBounds.width.truncate()},0"
                    + (1..5).map {
                " M ${(chartBounds.x + it * chartBounds.width / 6.0).truncate()},${chartBounds.y.truncate()} l 0,${chartBounds.height.truncate()}"
            }.joinToString(" "))
        grid.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        grid.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE,
            FILL_DARK_GREY
        )
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "0.58")
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        g.appendChild(grid)
        val startx = chartBounds.x - chartBounds.width / 12.0
        val starty = chartBounds.y + calcFontHeight(5.8f)
        val cons = listOf("3", "5", "7", "10", "11", bundle.getString("dead"))
        for (i in 1..6) {
            addTextElement(startx + i * chartBounds.width / 6.0, starty,
                i.toString(), 5.8f, SVGConstants.SVG_MIDDLE_VALUE, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY, parent = g)
            addTextElement(startx + i * chartBounds.width / 6.0, starty + chartBounds.height / 2.0,
                cons[i - 1], 5.8f, SVGConstants.SVG_MIDDLE_VALUE, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY, width = chartBounds.width / 6.0 - 4.0, parent = g)
        }
        addTextElement(chartBounds.x - padding, starty, bundle.getString("hitsTaken"),
            5.2f, SVGConstants.SVG_END_VALUE, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = g)
        addTextElement(chartBounds.x - padding, starty + chartBounds.height / 2.0, bundle.getString("consciousnessNum"),
            5.2f, SVGConstants.SVG_END_VALUE, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = g)
        parent.appendChild(g)
        return height
    }

    open fun addCrewData(crewIndex: Int, y: Double, width: Double, parent: Element): Double {
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = y + lineHeight * 1.5
        addField(bundle.getString("name"), "pilotName$crewIndex",
            padding, ypos, fontSize,
            blankId = "blankCrewName$crewIndex",
            blankWidth = width - padding * 2
                    - calcTextLength("${bundle.getString("name")}_", fontSize, fontWeight),
            labelId = "crewName$crewIndex", labelFixedWidth = false, hidden = hideCrewIndex(crewIndex),
            parent = parent)
        ypos += lineHeight * 1.5
        addField(bundle.getString("gunnerySkill"), "gunnerySkill$crewIndex",
            padding,
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
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(this, rect.width * 0.5, 0.0, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        g.appendChild(label.draw())
        val pipScale = embedImage(
            padding,
            padding, rect.width - padding, rect.height - padding, armorDiagramFileName, ImageAnchor.CENTER, g)[2] * 0.966
        val pipG = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        pipG.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "canonArmorPips")
        // The canon pip images are centered on 502.33,496.33 and need to be scaled 0.966 to fit the full-sized diagrams
        pipG.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_MATRIX_VALUE} (${pipScale.truncate()} 0 0 ${pipScale.truncate()},"
                + (padding + rect.width * 0.5 - 497.165 * pipScale).truncate()
                + " " + (padding + rect.height * 0.5 - 214.068 * pipScale).truncate() + ")")
        g.appendChild(pipG)
        document.documentElement.appendChild(g)
        document.getElementById("shieldRA")?.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE)
        document.getElementById("shieldLA")?.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE)
    }

    fun addCritTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("critTablePanel.title"), parent = g)
        val colWidth = internal.width / 3.0 - padding * 4.0
        val fontSize = 9.65f
        var ypos = internal.y + internal.height * 0.05
        if (isQuad()) {
            addSingleCritLocation(internal.x + padding, ypos + internal.height * 0.087,
                colWidth,internal.height * 0.145,"crits_FLL", g)
            addSingleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos + internal.height * 0.087,
                colWidth, internal.height * 0.145,"crits_FRL", g)
        } else {
            addDoubleCritLocation(internal.x + padding, ypos, colWidth, internal.height * 0.3,"crits_LA", fontSize, g)
            addDoubleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos, colWidth, internal.height * 0.3,"crits_RA", fontSize, g)
        }
        ypos += internal.height * 0.387
        addDoubleCritLocation(internal.x + padding, ypos, colWidth, internal.height * 0.3,"crits_LT", fontSize, g)
        addDoubleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos, colWidth, internal.height * 0.3, "crits_RT", fontSize, g)
        ypos += internal.height * 0.387
        addSingleCritLocation(internal.x + padding, ypos, colWidth, internal.height * 0.145,
            if (isQuad()) "crits_RLL" else "crits_LL", g)
        addSingleCritLocation(internal.x + colWidth * 2.0 + padding * 3.0, ypos, colWidth, internal.height * 0.145,
            if (isQuad()) "crits_RRL" else "crits_RL", g)
        if (isTripod()) {
            addSingleCritLocation(internal.x + colWidth + padding * 2.0, ypos, colWidth, internal.height * 0.145,
                "crits_CL", g)
        }
        ypos = internal.y + internal.height * 0.02
        addSingleCritLocation(internal.x + colWidth + padding * 2.0, ypos, colWidth, internal.height * 0.145, "crits_HD", g)
        ypos += internal.height * 0.215
        addDoubleCritLocation(internal.x + colWidth + padding * 2.0, ypos, colWidth, internal.height * 0.3, "crits_CT", fontSize, g)
        ypos += internal.height * 0.31
        ypos += addSystemPips(internal.x + colWidth + padding * 2.0, ypos, colWidth, g)
        addDamageTransferDiagram(internal.x + colWidth + padding * 2.0, ypos,
            colWidth, if (isTripod()) internal.height * 0.835 - ypos - padding else internal.height - ypos, g)
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
                              parent: Element) {
        addRect(x + 18.0, y, width - 18.0, height, id = id, parent = parent)
    }

    open fun addSystemPips(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = FONT_SIZE_FREE_LABEL
        val lineHeight = calcFontHeight(fontSize)
        val pipRadius = 2.8
        val pipDx = 9.2
        val textWidth = systems.map {
            calcTextLength("${it.first}_", fontSize, SVGConstants.SVG_BOLD_VALUE)
        }.max() ?: 0.0
        val textAnchor = textWidth + padding * 2
        var ypos = padding + lineHeight
        val contentWidth = textWidth + (pipRadius + pipDx) * 2 + padding * 4
        val gContent = createTranslatedGroup(x + (width - contentWidth) * 0.5, y)
        systems.forEach {
            addTextElement(
                textAnchor, ypos, it.first, fontSize, SVGConstants.SVG_BOLD_VALUE,
                FILL_DARK_GREY, SVGConstants.SVG_END_VALUE, fixedWidth = true, parent = gContent
            )
            for (i in 0 until it.second) {
                val pip = DrawPip(textAnchor + pipRadius + pipDx * i, ypos - pipRadius * 2,
                    pipRadius, 1.72).draw(document)
                // so hacky
                if (it.first.equals(bundle.getString("gyroHits")) && (i + 1 == it.second)) {
                    pip.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "heavyDutyGyroPip")
                    pip.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE)
                }
                gContent.appendChild(pip)
            }
            ypos += lineHeight
        }
        ypos = appendSystemCrits(ypos, width, pipDx * 2, fontSize, gContent)

        val border = RoundedBorder(
            padding * 2,
            padding, contentWidth, ypos - padding * 2, 6.78,
            3.75, 0.92).draw(document)
        gContent.appendChild(border)
        parent.appendChild(gContent)
        return ypos + padding
    }

    open fun addDamageTransferDiagram(x: Double, y: Double, width: Double, height: Double, parent: Element) {
        val lineHeight = calcFontHeight(FONT_SIZE_MEDIUM)
        embedImage(x + width * 0.5, y, width * 0.5, height - lineHeight,
            damageTransferFileName, ImageAnchor.CENTER, parent)
        addTextElement(x + width * 0.75, y + height, join(" ", bundle.getString("damageTransfer.1"), bundle.getString("damageTransfer.2")),
            FONT_SIZE_MEDIUM, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent)
        addTextElement(x + width * 0.75, y + height + lineHeight, bundle.getString("damageTransfer.3"),
            FONT_SIZE_MEDIUM, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent)
        embedImage(x, y, width * 0.5 - padding, height,
            CGL_LOGO, ImageAnchor.RIGHT, parent)
    }

    /**
     * Opportunity for LAMs to add the SI section
     */
    open fun appendSystemCrits(ypos: Double, width: Double, rectHeight: Double, fontSize: Float, parent: Element) = ypos

    fun addStructureDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(this, rect.width * 0.5, 0.0, bundle.getString("isPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        g.appendChild(label.draw())
        val pipScale = embedImage(0.0, label.height() + 1, rect.width, rect.height - label.height() - 2,
            isDiagramFileName, ImageAnchor.CENTER, parent = g)[2] * 0.966
        val pipG = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        pipG.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "canonStructurePips")
        pipG.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_MATRIX_VALUE} (${pipScale.truncate()} 0 0 ${pipScale.truncate()} "
                    + "${(rect.width * 0.5 - 478.445 * pipScale).truncate()},"
                    + "${(label.height() + 1 + (rect.height - label.height() - 2) * 0.5 - 489.6 * pipScale).truncate()})")
        g.appendChild(pipG)
        document.documentElement.appendChild(g)
    }

    fun addHeatPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("heatPanel.title"), parent = g)
        addHeatEffects(inner.x, inner.y, inner.width, inner.height, g)
        document.documentElement.appendChild(g)
    }

    fun addHeatScale(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        g.appendChild(createHeatScale(rect.height - padding))
        document.documentElement.appendChild(g)
    }

    override fun heatEffect(heatLevel: Int): String? = when (heatLevel) {
        30 -> bundle.getString("heat.autoShutdown")
        28 -> format(bundle.getString("heat.ammoExplosion"), 8)
        26 -> format(bundle.getString("heat.shutdown"), 10)
        25 -> format(bundle.getString("heat.mpReduction"), -5)
        24 -> format(bundle.getString("heat.fireMod"), 4)
        23 -> format(bundle.getString("heat.ammoExplosion"), 6)
        22 -> format(bundle.getString("heat.shutdown"), 8)
        20 -> format(bundle.getString("heat.mpReduction"), -4)
        19 -> format(bundle.getString("heat.ammoExplosion"), 4)
        18 -> format(bundle.getString("heat.shutdown"), 6)
        17 -> format(bundle.getString("heat.fireMod"), 3)
        15 -> format(bundle.getString("heat.mpReduction"), -3)
        14 -> format(bundle.getString("heat.shutdown"), 4)
        13 -> format(bundle.getString("heat.fireMod"), 2)
        10 -> format(bundle.getString("heat.mpReduction"), -2)
        8 -> format(bundle.getString("heat.fireMod"), 1)
        5 -> format(bundle.getString("heat.mpReduction"), -1)
        else -> null
    }
}

class BipedMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_biped_default.svg"
    override val damageTransferFileName = "damage_transfer_biped.svg"
    override val armorDiagramFileName = "armor_diagram_biped.svg"
    override val isDiagramFileName = "internal_diagram_biped.svg"
}

class QuadMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_quad_default.svg"
    override val damageTransferFileName = "damage_transfer_quad.svg"
    override fun isQuad() = true
    override val armorDiagramFileName = "armor_diagram_quad.svg"
    override val isDiagramFileName = "internal_diagram_quad.svg"
}

class TripodMechRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_tripod_default.svg"
    override val damageTransferFileName = "damage_transfer_tripod.svg"
    override val armorDiagramFileName = "armor_diagram_tripod.svg"
    override val isDiagramFileName = "internal_diagram_tripod.svg"

    override fun isTripod() = true

    override fun maxCrew() = 3

    override fun hideCrewIndex(i: Int) = i > 1

    override fun addDamageTransferDiagram(x: Double, y: Double, width: Double, height: Double, parent: Element) {
        val lineHeight = calcFontHeight(FONT_SIZE_MEDIUM)
        embedImage(x + width * 0.7 + padding, y, width * 0.3, height + lineHeight,
            "damage_transfer_tripod.svg", ImageAnchor.LEFT, parent)
        addTextElement(x + width * 0.55 + padding, y + height * 0.5 - lineHeight * 0.5, bundle.getString("damageTransfer.1"),
            FONT_SIZE_MEDIUM, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent)
        addTextElement(x + width * 0.55 + padding, y + height * 0.5 + lineHeight * 0.5, bundle.getString("damageTransfer.2"),
            FONT_SIZE_MEDIUM, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent)
        addTextElement(x + width * 0.55 + padding, y + height * 0.5 + lineHeight * 1.5, bundle.getString("damageTransfer.3"),
            FONT_SIZE_MEDIUM, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent)
        embedImage(x, y, width * 0.4 + padding, height,
            CGL_LOGO, ImageAnchor.RIGHT, parent)
    }
}

class LAMRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_biped_lam.svg"
    override val damageTransferFileName = "damage_transfer_biped.svg"
    override val armorDiagramFileName = "armor_diagram_biped.svg"
    override val isDiagramFileName = "internal_diagram_biped.svg"

    override val systems = listOf(
        Pair(bundle.getString("avionicsHits"), 3),
        Pair(bundle.getString("engineHits"), 3),
        Pair(bundle.getString("gyroHits"), 3),
        Pair(bundle.getString("sensorHits"), 2),
        Pair(bundle.getString("landingGear"), 1),
        Pair(bundle.getString("lifeSupport"), 1)
    )

    override fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        addField(
            bundle.getString("tonnage"), "tonnage", x, y, fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent
        )
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("techBase"), "techBase", "Inner Sphere"),
                LabeledField(bundle.getString("rulesLevel"), "rulesLevel", "Standard"),
                LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
            ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, parent = parent
        )

        addTextElement(
            x, y + lineHeight, bundle.getString("movementPoints"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent
        )
        addTextElement(
            x, y + lineHeight * 2, bundle.getString("battlemech"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent
        )
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("walking"), "mpWalk", "0"),
                LabeledField(bundle.getString("running"), "mpRun", "0"),
                LabeledField(bundle.getString("jumping"), "mpJump", "0",
                    labelId = "lblJump")
            ), x, y + lineHeight * 3, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent
        )

        addTextElement(
            x + width * 0.48, y + lineHeight * 3, bundle.getString("airmech"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = parent
        )
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("walking"), "mpAirMechWalk", "0"),
                LabeledField(bundle.getString("running"), "mpAirMechRun", "0")
            ), x + width * 0.24, y + lineHeight * 4, fontSize,
            FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent
        )
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("cruising"), "mpAirMechCruise", "0"),
                LabeledField(bundle.getString("flanking"), "mpAirMechFlank", "0")
            ), x + width * 0.48, y + lineHeight * 4, fontSize,
            FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent
        )

        addTextElement(
            x + width * 0.72, y + lineHeight * 3, bundle.getString("fighter"), fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, parent = parent
        )
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("safeThrust"), "mpSafeThrust", "0"),
                LabeledField(bundle.getString("maxThrust"), "mpMaxThrust", "0")
            ), x + width * 0.72, y + lineHeight * 4, fontSize,
            FILL_DARK_GREY, 47.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent
        )

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
        addField(
            bundle.getString("name"), "pilotName$crewIndex",
            padding, ypos, fontSize,
            blankId = "blankCrewName$crewIndex",
            blankWidth = width - padding * 2
                    - calcTextLength("${bundle.getString("name")}_", fontSize, fontWeight),
            labelId = "crewName$crewIndex", labelFixedWidth = false, hidden = hideCrewIndex(crewIndex),
            parent = parent
        )
        ypos += lineHeight * 1.5
        addTextElement(
            padding, ypos, bundle.getString("battlemech"), fontSize,
            fontWeight = fontWeight, parent = parent)
        ypos += lineHeight
        addField(
            bundle.getString("gunnerySkill"), "gunnerySkill$crewIndex",
            padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "blankGunnerySkill$crewIndex", labelId = "gunnerySkillText$crewIndex",
            blankWidth = width * 0.13, hidden = hideCrewIndex(crewIndex),
            parent = parent
        )
        addField(
            bundle.getString("pilotingSkill"), "pilotingSkill$crewIndex", width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "blankPilotingSkill$crewIndex", labelId = "pilotingSkillText$crewIndex",
            blankWidth = width * 0.18 - padding, hidden = hideCrewIndex(crewIndex),
            parent = parent
        )
        ypos += lineHeight
        addTextElement(
            padding, ypos, bundle.getString("aerospace"), fontSize,
            fontWeight = fontWeight, parent = parent)
        ypos += lineHeight
        addField(
            bundle.getString("gunnerySkill"), "asfGunnerySkill",
            padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "asfBlankGunnerySkill", labelId = "asfGunnerySkillText",
            blankWidth = width * 0.13, parent = parent
        )
        addField(
            bundle.getString("pilotingSkill"), "asfPilotingSkill", width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = width * 0.32,
            blankId = "asfBlankPilotingSkill", labelId = "asfPilotingSkillText",
            blankWidth = width * 0.18 - padding, parent = parent
        )
        ypos += lineHeight
        return ypos
    }

    override fun appendSystemCrits(
        ypos: Double,
        width: Double,
        rectHeight: Double,
        fontSize: Float,
        parent: Element
    ): Double {
        addTextElement(
            width * 0.5, ypos, bundle.getString("structuralIntegrity"),
            fontSize, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent
        )
        addRect(
            padding * 4, ypos + padding, width - padding * 8, rectHeight,
            id = "siPips", parent = parent)
        return ypos + calcFontHeight(fontSize) + rectHeight + padding
    }

    override fun addDamageTransferDiagram(x: Double, y: Double, width: Double, height: Double, parent: Element) {
        val lineHeight = calcFontHeight(FONT_SIZE_MEDIUM)
        embedImage(
            x + width * 0.5, y, width * 0.5, height + lineHeight,
            "damage_transfer_biped.svg", ImageAnchor.TOP, parent
        )
        addTextElement(
            x + width * 0.25,
            y + height,
            join(" ", bundle.getString("damageTransfer.1"), bundle.getString("damageTransfer.2")),
            FONT_SIZE_MEDIUM,
            SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY,
            SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent
        )
        addTextElement(
            x + width * 0.25, y + height + lineHeight, bundle.getString("damageTransfer.3"),
            FONT_SIZE_MEDIUM, SVGConstants.SVG_BOLD_VALUE,
            FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE,
            parent = parent
        )
        embedImage(x, y, width * 0.5 - padding, height - lineHeight,
            CGL_LOGO, ImageAnchor.CENTER, parent)
    }

    override fun heatEffect(heatLevel: Int): String? = when (heatLevel) {
        25 -> format(bundle.getString("heat.lamMPReduction"), 5, 10)
        20 -> format(bundle.getString("heat.lamMPReduction"), 4, 8)
        15 -> format(bundle.getString("heat.lamMPReduction"), 3, 7)
        10 -> format(bundle.getString("heat.lamMPReduction"), 2, 6)
        5 -> format(bundle.getString("heat.lamMPReduction"), 1, 5)
        else -> super.heatEffect(heatLevel)
    }
}

class QuadVeeRecordSheet(size: PaperSize) : MechRecordSheet(size) {
    override val fileName = "mech_quadvee.svg"
    override val damageTransferFileName = "damage_transfer_quadvee.svg"
    override val armorDiagramFileName = "armor_diagram_quadvee.svg"
    override val isDiagramFileName = "internal_diagram_quadvee.svg"

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
        ), x + width * 0.25, y + lineHeight * 2, fontSize,
            FILL_DARK_GREY, 38.0,
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