package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 * Base class for aerospace record sheets
 */
abstract class AeroRecordSheet(size: PaperSize): RecordSheet(size) {

    private val eqTableCell = Cell(0.0, logoHeight + titleHeight,
        width() * 0.4, height() * 0.5 - logoHeight - titleHeight)
    private val armorCell = Cell(eqTableCell.rightX() + padding, 0.0,
        width() * 0.6 - padding, height() * 0.65 - padding)
    private val fluffCell = Cell(0.0, eqTableCell.bottomY() + padding,
        eqTableCell.width, armorCell.bottomY() - eqTableCell.bottomY() - padding)
    private val tableCell = Cell(0.0, armorCell.bottomY() + padding,
        width(), height() * 0.35 - footerHeight - padding)
    private val critDamageCell = Cell(0.0, fluffCell.bottomY() + padding,
        eqTableCell.width, height() * 0.12)
    private val velocityCell = Cell(0.0, critDamageCell.bottomY() + padding,
        width() * 2.0 / 3.0, height() * 0.23 - footerHeight - padding)
    private val pilotCell = Cell(critDamageCell.rightX() + padding, critDamageCell.y,
        velocityCell.width - critDamageCell.width - padding, critDamageCell.height + tabBevelY)
    private val heatScaleCell = Cell(armorCell.rightX() - 20, (height() - footerHeight) / 2.0,
        20.0, (height() - footerHeight) / 2.0)
    private val heatCell = Cell(velocityCell.rightX() + padding, tableCell.y,
        width() - velocityCell.width - heatScaleCell.width - padding, height() - armorCell.height - footerHeight - padding)
    private val groundMovementCell = Cell(heatCell.x, heatCell.y,
        width() - velocityCell.width - padding, heatCell.height * 0.65)
    private val fighterReturnCell = Cell(heatCell.x, groundMovementCell.bottomY() + padding,
        groundMovementCell.width, heatCell.height - groundMovementCell.height - padding)

    private val largeCraftEqTableCell = Cell(eqTableCell.x, eqTableCell.y, eqTableCell.width,
        fluffCell.bottomY() - eqTableCell.y)
    private val largeCraftFluffCell = critDamageCell
    private val largeCraftCritDamageCell = groundMovementCell
    private val largeCraftHeatCell = fighterReturnCell

    protected val bundle: ResourceBundle = ResourceBundle.getBundle(AeroRecordSheet::class.java.name)

    final override fun height() = super.height()
    abstract val dataPanelTitle: String
    abstract val armorDiagramFileName: String

    abstract val fighter: Boolean
    abstract val tracksHeat: Boolean
    abstract val largeCraft: Boolean
    override fun colorElements() = "${super.colorElements()},heatScale"

    open fun isAtmospheric(): Boolean = false
    open fun isCapitalScale(): Boolean = false
    open fun isWarship(): Boolean = false
    open fun isAerodyne(): Boolean = false
    open fun stationKeeping(): Boolean = false
    open fun isStation(): Boolean = false

    override fun build() {
        if (largeCraft) {
            addEquipmentTable(largeCraftEqTableCell)
            addFluffPanel(largeCraftFluffCell)
            addArmorDiagram(armorCell)
            addLCCritPanel(largeCraftCritDamageCell)
            addLCPilotPanel(pilotCell)
            addVelocityPanel(velocityCell)
            addLCHeatPanel(largeCraftHeatCell)
        } else {
            addEquipmentTable(eqTableCell)
            addFluffPanel(fluffCell)
            addArmorDiagram(armorCell)
            addCritPanel(critDamageCell)
            addPilotPanel(pilotCell)
            addVelocityPanel(velocityCell)
            if (tracksHeat) {
                addHeatPanel(heatCell)
                addHeatScale(heatScaleCell)
            } else if (fighter) {
                addGroundMovementTable(groundMovementCell)
                addFighterReturnTable(fighterReturnCell)
            }
            if (fighter) {
                addBombsPanel()
            }
        }
    }

    private fun addEquipmentTable(rect: Cell) {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x.truncate()},${rect.y.truncate()})")
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unitDataPanel")
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            dataPanelTitle, topTab = true, bottomTab = true, bevelTopRight = false,
            textBelow = bundle.getString("notes.title"), parent = g)
        var ypos = internal.y
        val fontSize = 9.67f
        val lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x +padding,
            ypos, fontSize, maxWidth = internal.width - internal.x - padding, parent = g)
        ypos += lineHeight
        ypos += addUnitDataFields(internal.x + padding, ypos, internal.width, parent = g)
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, bundle.getString("weaponsAndEquipment"),
            FONT_SIZE_FREE_LABEL,
            SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, width = internal.width * 0.6, parent = g)

        addRect(internal.x, ypos, internal.width - padding, internal.bottomY() - ypos - lineHeight * 2.0,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g)

        addHorizontalLine(internal.x, internal.bottomY() - padding - lineHeight * 1.5, internal.width - padding, parent = g)
        addField(bundle.getString("bv"), "bv", internal.x + padding + bevelX, internal.bottomY() - padding * 2,
            fontSize, defaultText = "0", parent = g)
        addRect(rect.width * 0.5 + (rect.width * 0.5 - tabBevelX - padding) * 0.5 - 10,
            internal.bottomY() - padding * 0.5 - lineHeight * 0.75 + tabBevelY * 0.5 - 10.0,
            20.0, 20.0, id = "eraIcon", parent = g)
        rootElement.appendChild(g)
    }

    private fun addFluffPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "notes")
        addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("notes.title"), bottomTab = true,
            textBelow = bundle.getString(when {
                    isStation() -> "notes.title"
                    largeCraft -> "velocityRecord.title"
                    else -> "criticalDamage.title"
                }), parent = g)
        rootElement.appendChild(g)
        val fluffCell = rect.inset(
            padding,
            padding,
            padding,
            padding
        )
        addRect(fluffCell.x, fluffCell.y, fluffCell.width, fluffCell.height,
            stroke = SVGConstants.SVG_NONE_VALUE, id = "fluffImage")
    }

    /**
     * Adds the fields and labels at the top of the unit data panel
     */
    open fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        var ypos = y
        if (largeCraft) {
            addField(bundle.getString("name"), "fluffName", x, ypos,
                fontSize, blankId = "blankFluffName",
                blankWidth = width * 0.45 - calcTextLength(bundle.getString("name") + "_", fontSize),
                parent = parent)
            ypos += lineHeight
        }
        addTextElement(x, ypos, bundle.getString("thrust"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, parent = parent)
        ypos += lineHeight
        if (stationKeeping()) {
            addTextElement(x + calcTextLength("_", fontSize), ypos, bundle.getString("stationKeeping"),
                fontSize, parent = parent)
        } else {
            addFieldSet(listOf(
                LabeledField(bundle.getString("safeThrust"), "mpWalk", "0"),
                LabeledField(bundle.getString("maxThrust"), "mpRun", "0")
            ), x + calcTextLength("_", fontSize), ypos, fontSize, FILL_DARK_GREY,
                70.0, SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        }
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, maxWidth = width * 0.5 - padding, parent = parent)
        return lineHeight * 4
    }

    private fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val rightMargin = if (largeCraft) 0.0 else heatScaleCell.width
        if (fighter) {
            val label = RSLabel(
                this, 0.0, logoHeight + titleHeight,
                bundle.getString("armorPanel.title"), FONT_SIZE_FREE_LABEL
            )
            g.appendChild(label.draw())
            embedImage(0.0, logoHeight * 0.5, rect.width - rightMargin,
                rect.height - logoHeight * 0.5,
                armorDiagramFileName, ImageAnchor.CENTER, parent = g)
            embedImage(rect.width - 50.0 - rightMargin - padding, rect.height - CGL_LOGO_HEIGHT,
                CGL_LOGO_WIDTH, CGL_LOGO_HEIGHT, CGL_LOGO, CGL_LOGO_BW, anchor = ImageAnchor.BOTTOM_RIGHT,
                id = "cglLogo", parent = g)
        } else {
            val label = RSLabel(
                this, rect.width, 0.0,
                bundle.getString("armorPanel.title"), FONT_SIZE_FREE_LABEL,
                right = true
            )
            g.appendChild(label.draw())
            val labelCenterX = rect.width - (label.rectWidth + label.taperWidth) * 0.5
            addTextElement(labelCenterX,label.height() + calcFontHeight(FONT_SIZE_SMALL),
                bundle.getString(if (isCapitalScale()) "capitalScale" else "standardScale"),
                FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
            embedImage(
                0.0, logoHeight * 0.5, rect.width - rightMargin,
                rect.height - logoHeight * 0.5 - padding,
                armorDiagramFileName, ImageAnchor.CENTER, parent = g
            )
            embedImage(labelCenterX - 25.0,label.height() + 30, CGL_LOGO_WIDTH, CGL_LOGO_HEIGHT,
                CGL_LOGO, CGL_LOGO_BW, id = "cglLogo", parent = g)
        }
        if (!isAtmospheric() && !stationKeeping()) {
            addAeroMovementCompass(Cell(0.0, rect.height - 50.0, 90.0, 50.0), parent = g)
        }
        rootElement.appendChild(g)
    }

    private fun addCritPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("criticalDamage.title"), bottomTab = true,
            textBelow = bundle.getString("velocityRecord.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = inner.height / 4.0
        var ypos = inner.y + lineHeight * 0.5
        val boxHeight = calcFontHeight(fontSize) * 1.5
        g.appendChild(DamageCheckBox(bundle.getString("avionics"), listOf("+1", "+2", "+5"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.45))
        g.appendChild(DamageCheckBox(bundle.getString("engine"), listOf("2", "4", "D"),
            boxHeight = boxHeight)
            .draw(this, inner.x + inner.width * 0.5, ypos,
                fontSize, width = inner.width * 0.45))
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("fcs"), listOf("+2", "+4", "D"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.45))
        g.appendChild(DamageCheckBox(bundle.getString("gear"), listOf("+5"),
            boxHeight = boxHeight)
            .draw(this, inner.x + inner.width * 0.5, ypos,
                fontSize, width = inner.width * 0.45))
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("sensors"), listOf("+1", "+2", "+5"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.45))
        g.appendChild(DamageCheckBox(bundle.getString("lifeSupport"), listOf("+2"),
            boxHeight = boxHeight)
            .draw(this, inner.x + inner.width * 0.5, ypos,
                fontSize, width = inner.width * 0.45))
        rootElement.appendChild(g)
    }

    private fun addLCCritPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("criticalDamage.title"), bottomTab = true,
            textBelow = bundle.getString("heatPanel.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = inner.height / if (isStation()) 7.0 else 8.0
        var ypos = inner.y + lineHeight * 0.5
        val boxHeight = calcFontHeight(fontSize) * 1.5
        val col2X = inner.x + inner.width * 0.63
        g.appendChild(DamageCheckBox(bundle.getString("avionics"), listOf("+1", "+2", "+5"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.57,
            fontWeight = SVGConstants.SVG_BOLD_VALUE))
        if (isCapitalScale()) {
            g.appendChild(DamageCheckBox(bundle.getString("lifeSupport"), listOf("+2"),
                boxHeight = boxHeight)
                .draw(this, col2X, ypos, fontSize, width = inner.width * 0.35,
                    fontWeight = SVGConstants.SVG_BOLD_VALUE))
        } else {
            g.appendChild(DamageCheckBox(bundle.getString("gear"), listOf("+5"),
            boxHeight = boxHeight)
            .draw(this, col2X, ypos, fontSize, width = inner.width * 0.35,
                fontWeight = SVGConstants.SVG_BOLD_VALUE))
        }
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString(if (isCapitalScale()) "cic" else "fcs"), listOf("2", "4", "D"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding, ypos,
                fontSize, width = inner.width * 0.57,
            fontWeight = SVGConstants.SVG_BOLD_VALUE))
        if (!isCapitalScale()) {
            g.appendChild(DamageCheckBox(bundle.getString("lifeSupport"), listOf("+2"),
                boxHeight = boxHeight)
                .draw(this, col2X, ypos, fontSize, width = inner.width * 0.35,
                    fontWeight = SVGConstants.SVG_BOLD_VALUE))
        }
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("sensors"), listOf("+1", "+2", "+5"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.57,
                fontWeight = SVGConstants.SVG_BOLD_VALUE))
        if (!isCapitalScale()) {
            g.appendChild(DamageCheckBox(bundle.getString("kfBoom"), listOf("D"),
                boxHeight = boxHeight)
                .draw(this, col2X, ypos, fontSize, width = inner.width * 0.35,
                    fontWeight = SVGConstants.SVG_BOLD_VALUE))
        }
        ypos += lineHeight
        addTextElement(inner.x + padding, ypos + boxHeight * 0.9, bundle.getString("thrusters"),
            fontSize, fontWeight = SVGConstants.SVG_BOLD_VALUE, parent = g)
        if (!isCapitalScale()) {
            g.appendChild(DamageCheckBox(bundle.getString("dockingCollar"), listOf("D"),
                boxHeight = boxHeight)
                .draw(this, col2X, ypos,
                    fontSize, width = inner.width * 0.35,
                    fontWeight = SVGConstants.SVG_BOLD_VALUE))
        }
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("left"), listOf("+1", "+2", "+3", "D"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding * 5, ypos, fontSize,
                width = inner.width * 0.57 + boxHeight - padding * 3,
                fontWeight = SVGConstants.SVG_BOLD_VALUE))
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("right"), listOf("+1", "+2", "+3", "D"),
            boxHeight = boxHeight)
            .draw(this, inner.x + padding * 5, ypos, fontSize,
                width = inner.width * 0.57 + boxHeight - padding * 3,
                fontWeight = SVGConstants.SVG_BOLD_VALUE))
        ypos += lineHeight
        if (!isStation()) {
            g.appendChild(DamageCheckBox(bundle.getString("engine"), listOf("-1", "-2", "-3", "-4", "-5", "D"),
                boxHeight = boxHeight)
                .draw(this, inner.x + padding, ypos, fontSize,
                    width = inner.width * 0.57 + (boxHeight + padding) * 3,
                    fontWeight = SVGConstants.SVG_BOLD_VALUE))
        }
        rootElement.appendChild(g)
    }

    private fun addPilotPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("pilotPanel.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = inner.y + lineHeight * 1.5
        addField(bundle.getString("name"), "pilotName0",
            padding, ypos, fontSize,
            blankId = "blankCrewName0",
            blankWidth = inner.width - padding * 2
                    - calcTextLength("${bundle.getString("name")}_",
                fontSize, fontWeight),
            labelFixedWidth = false, parent = g)
        ypos += lineHeight * 1.5
        addField(bundle.getString("gunnerySkill"), "gunnerySkill0",
            padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankGunnerySkill0", labelId = "gunnerySkillText0",
            blankWidth = inner.width * 0.13, parent = g)
        addField(bundle.getString("pilotingSkill"), "pilotingSkill0", inner.width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankPilotingSkill0", labelId = "pilotingSkillText0",
            blankWidth = inner.width * 0.18 - padding, parent = g)
        ypos += lineHeight
        addPilotDamageTrack(0.0, ypos, inner.width, parent = g)
        rootElement.appendChild(g)
    }

    private fun addPilotDamageTrack(x: Double, y: Double, width: Double, height: Double = 20.0,
                                    parent: Element): Double {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        val chartBounds = Cell(x + width * 0.35, y,
            width * 0.65 - padding,30.0)
        val outline = drawPilotDamageOutline(chartBounds.x, chartBounds.y, chartBounds.width, chartBounds.height)
        g.appendChild(outline)
        val grid = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        grid.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${chartBounds.x.truncate()},${(chartBounds.y + chartBounds.height / 3.0).truncate()}"
                    + " h ${chartBounds.width.truncate()}"
                    + "M ${chartBounds.x.truncate()},${(chartBounds.y + chartBounds.height * 2.0 / 3.0).truncate()}"
                    + " h ${(chartBounds.width * 5.0 / 6.0).truncate()}"
                    + (1..5).joinToString(" ") {
                " M ${(chartBounds.x + it * chartBounds.width / 6.0).truncate()},${chartBounds.y.truncate()} l 0,${chartBounds.height.truncate()}"
            })
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
                i.toString(), 5.8f, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
            addTextElement(startx + i * chartBounds.width / 6.0, starty + chartBounds.height / 3.0,
                cons[i - 1], 5.8f, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, width = chartBounds.width / 6.0 - 4.0, parent = g)
            if (i < 6) {
                addTextElement(
                    startx + i * chartBounds.width / 6.0, starty + chartBounds.height * 2.0 / 3.0,
                    "+$i", 5.8f, SVGConstants.SVG_BOLD_VALUE,
                    anchor = SVGConstants.SVG_MIDDLE_VALUE, width = chartBounds.width / 6.0 - 4.0, parent = g
                )
            }
        }
        addTextElement(chartBounds.x - padding, starty, bundle.getString("hitsTaken"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g)
        addTextElement(chartBounds.x - padding, starty + chartBounds.height / 3.0, bundle.getString("consciousnessNum"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g)
        addTextElement(chartBounds.x - padding, starty + chartBounds.height * 2.0 / 3.0, bundle.getString("modifier"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g)
        parent.appendChild(g)
        return height
    }

    private fun drawPilotDamageOutline(x: Double, y: Double, width: Double, height: Double): Element {
        val outline = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        outline.setAttributeNS(null, SVGConstants.CSS_FILL_PROPERTY, SVGConstants.SVG_NONE_VALUE)
        outline.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, FILL_DARK_GREY)
        outline.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "1")
        outline.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        val radius = 1.015
        val control = 0.56
        outline.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${x.truncate()},${(y + radius).truncate()}"
                    + " c 0,${(-control).truncate()} ${(radius - control).truncate()},${(-radius).truncate()} ${radius.truncate()},${(-radius).truncate()}"
                    + " h ${(width - radius * 2).truncate()} c ${control.truncate()},0 ${radius.truncate()},${(radius - control).truncate()}, ${radius.truncate()},${radius.truncate()}"
                    + " v ${(height * 2.0 / 3.0 - radius * 2).truncate()} c 0,${control.truncate()} ${(control - radius).truncate()},${radius.truncate()}, ${(-radius).truncate()},${radius.truncate()}"
                    + " h -${width / 6.0 - radius} v ${(height / 3.0 - radius).truncate()} c 0,${control.truncate()} ${(control - radius).truncate()},${radius.truncate()}, ${(-radius).truncate()},${radius.truncate()}"
                    + " h -${(width * 5.0 / 6.0 - radius * 2).truncate()} c ${(-control).truncate()},0 ${(-radius).truncate()},${(control - radius).truncate()}, ${(-radius).truncate()},${(-radius).truncate()}"
                    + "Z"
        )
        return outline
    }

    private fun addLCPilotPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("pilotPanel.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = (inner.height - 20.0) / 7.0
        var ypos = inner.y + lineHeight * 1.5
        addField(bundle.getString("gunnerySkill"), "gunnerySkill0",
            padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankGunnerySkill0", labelId = "gunnerySkillText0",
            blankWidth = inner.width * 0.13, parent = g)
        addField(bundle.getString("pilotingSkill"), "pilotingSkill0", inner.width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankPilotingSkill0", labelId = "pilotingSkillText0",
            blankWidth = inner.width * 0.18 - padding, parent = g)
        ypos += lineHeight * 0.75
        ypos += addCrewDamageTrack(0.0, ypos, inner.width, parent = g) + lineHeight * 1
        addFieldSet(listOf(
            LabeledField(bundle.getString("nCrew"), "nCrew", "0"),
            LabeledField(bundle.getString("nPassengers"), "nPassengers", "0"),
            LabeledField(bundle.getString("nOther"), "nOther", "0")
        ), inner.x + padding, ypos, fontSize, fieldOffset = inner.width * 0.4,
            fieldAnchor = SVGConstants.SVG_END_VALUE, parent = g)
        addFieldSet(listOf(
            LabeledField(bundle.getString("nMarines"), "nMarines", "0"),
            LabeledField(bundle.getString("nBA"), "nBattleArmor", "0", labelId = "lblBattleArmor")
        ), inner.x + inner.width * 0.5, ypos, fontSize, fieldOffset = inner.width * 0.4,
            fieldAnchor = SVGConstants.SVG_END_VALUE, parent = g)
        ypos += lineHeight * 3.2
        addTextElement(inner.x + inner.width * 0.5, ypos, bundle.getString("lifeBoats"),
            fontSize, fontWeight, anchor = SVGConstants.SVG_MIDDLE_VALUE, id="lifeBoatsEscapePods",
            fixedWidth = true, parent = g)
        rootElement.appendChild(g)
    }

    fun addCrewDamageTrack(x: Double, y: Double, width: Double, height: Double = 20.0,
                           parent: Element): Double {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
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
        val mods = listOf("+1", "+2", "+3", "+4", "+5", bundle.getString("incapacitated"))
        for (i in 1..6) {
            addTextElement(startx + i * chartBounds.width / 6.0, starty,
                i.toString(), 5.8f, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
            addTextElement(startx + i * chartBounds.width / 6.0, starty + chartBounds.height / 2.0,
                mods[i - 1], 5.8f, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, width = chartBounds.width / 6.0 - 4.0, parent = g)
        }
        addTextElement(chartBounds.x - padding, starty, bundle.getString("hitsTaken"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g)
        addTextElement(chartBounds.x - padding, starty + chartBounds.height / 2.0, bundle.getString("modifier"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g)
        parent.appendChild(g)
        return height
    }

    private fun addVelocityPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        if (isStation()) {
            addBorder(0.0, 0.0, rect.width, rect.height,
                bundle.getString("notes.title"), topTab = true, bottomTab = false,
                parent = g)
        } else {
            val inner = addBorder(0.0, 0.0, rect.width, rect.height,
                bundle.getString("velocityRecord.title"), topTab = true, bottomTab = false,
                parent = g)
            addVelocityTrack(padding * 1.5, inner.y + inner.height * 0.1,
                inner.width - padding * 2, inner.height * 0.3, (1..10).toList(), g)
            addVelocityTrack(padding * 1.5, inner.y + inner.height * 0.55,
                inner.width - padding * 2, inner.height * 0.3, (11..20).toList(), g)
        }
        rootElement.appendChild(g)
    }

    private fun addVelocityTrack(x: Double, y: Double,
                                  width: Double, height: Double, range: List<Int>, parent: Element) {
        val outline = RoundedBorder(x, y, width, height,
            1.315, 0.726, 1.0).draw(document)
        parent.appendChild(outline)
        val grid = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        val colOffset = x + width * 0.2
        val colWidth = (width - colOffset) / range.size
        grid.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            (1..4).joinToString(" ") {
                ("M ${x.truncate()},${(y + height * 0.2 * it).truncate()}"
                        + " h${width.truncate()}")
            }
                    + (range.indices).joinToString(" ") {
                " M ${(colOffset + it * colWidth).truncate()},${y.truncate()} v${height.truncate()}"
            })
        grid.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        grid.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE,
            FILL_DARK_GREY
        )
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "0.58")
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        parent.appendChild(grid)
        val fontSize = FONT_SIZE_MEDIUM
        val startX = colOffset + colWidth * 0.5
        val startY = y + (height - calcFontHeight(fontSize)) * 0.5 - 1 - height * 0.2
        addTextElement(x + padding, startY, bundle.getString("turnNum"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addTextElement(x + padding, height * 0.2 + startY, bundle.getString("thrustRecord"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addTextElement(x + padding, height * 0.4 + startY, bundle.getString("velocity"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addTextElement(x + padding, height * 0.6 + startY, bundle.getString("effectiveVelocity"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addTextElement(x + padding, height * 0.8 + startY, bundle.getString("altitude"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        for (i in range.indices) {
            addTextElement(startX + i * colWidth, startY, range[i].toString(), fontSize,
                SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
                parent = parent)
        }
    }

    open fun addHeatPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("heatPanel.title"), parent = g)
        addHeatEffects(inner.x, inner.y, inner.width, inner.height, g)
        rootElement.appendChild(g)
    }

    open fun addHeatScale(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        g.appendChild(createHeatScale(rect.height - padding, true))
        g.appendChild(createHeatScale(rect.height - padding, false))
        rootElement.appendChild(g)
    }

    override fun heatEffect(heatLevel: Int): String? = when (heatLevel) {
        30 -> bundle.getString("heat.autoShutdown")
        28 -> java.lang.String.format(bundle.getString("heat.ammoExplosion"), 8)
        27 -> java.lang.String.format(bundle.getString("heat.pilotDamage"), 9)
        26 -> java.lang.String.format(bundle.getString("heat.shutdown"), 10)
        25 -> java.lang.String.format(bundle.getString("heat.randomMovement"), 10)
        24 -> java.lang.String.format(bundle.getString("heat.fireMod"), 4)
        23 -> java.lang.String.format(bundle.getString("heat.ammoExplosion"), 6)
        22 -> java.lang.String.format(bundle.getString("heat.shutdown"), 8)
        21 -> java.lang.String.format(bundle.getString("heat.pilotDamage"), 6)
        20 -> java.lang.String.format(bundle.getString("heat.randomMovement"), 8)
        19 -> java.lang.String.format(bundle.getString("heat.ammoExplosion"), 4)
        18 -> java.lang.String.format(bundle.getString("heat.shutdown"), 6)
        17 -> java.lang.String.format(bundle.getString("heat.fireMod"), 3)
        15 -> java.lang.String.format(bundle.getString("heat.randomMovement"), 7)
        14 -> java.lang.String.format(bundle.getString("heat.shutdown"), 4)
        13 -> java.lang.String.format(bundle.getString("heat.fireMod"), 2)
        10 -> java.lang.String.format(bundle.getString("heat.randomMovement"), 6)
        8 -> java.lang.String.format(bundle.getString("heat.fireMod"), 1)
        5 -> java.lang.String.format(bundle.getString("heat.randomMovement"), 5)
        else -> null
    }

    private fun addLCHeatPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("heatPanel.title"), parent = g)
        val col1Height = calcFontHeight(FONT_SIZE_VLARGE)
        addTextElement(inner.x + padding, inner.y + col1Height, bundle.getString("heatSinks"),
            FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, parent = g)
        addTextElement(inner.x + inner.width * 0.15, inner.y + col1Height * 2,
            "0", FONT_SIZE_VLARGE, SVGConstants.SVG_BOLD_VALUE, id="hsCount",
            anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(inner.x + inner.width * 0.15, inner.y + col1Height * 3,
            "(0)", FONT_SIZE_VLARGE, SVGConstants.SVG_BOLD_VALUE, id="hsCountDouble",
            anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        val lineHeight = inner.height / if (isWarship()) 7 else 6
        addTextElement(inner.x + inner.width * 0.35, inner.y + col1Height,
            bundle.getString("heatGenerationPerArc"), FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            fixedWidth = true, parent = g)
        if (isWarship()) {
            addFieldSet(
                listOf(
                    LabeledField(bundle.getString("nose"), "noseHeat", "0"),
                    LabeledField(bundle.getString("foreSides"), "foreSidesHeat", "0/0"),
                    LabeledField(bundle.getString("broadsides"), "broadsidesHeat", "0/0"),
                    LabeledField(bundle.getString("aftSides"), "aftSidesHeat", "0/0"),
                    LabeledField(bundle.getString("aft"), "aftHeat", "0")
                ), inner.x + inner.width * 0.35, inner.y + col1Height + lineHeight,
                FONT_SIZE_MEDIUM, fieldAnchor = SVGConstants.SVG_MIDDLE_VALUE,
                fieldOffset = inner.width * 0.5, parent = g)
        } else if (isAerodyne()) {
            addFieldSet(
                listOf(
                    LabeledField(bundle.getString("nose"), "noseHeat", "0"),
                    LabeledField(bundle.getString("wings"), "foreSidesHeat", "0/0"),
                    LabeledField(bundle.getString("wingsRear"), "aftSidesHeat", "0/0"),
                    LabeledField(bundle.getString("aft"), "aftHeat", "0")
                ), inner.x + inner.width * 0.35, inner.y + col1Height + lineHeight,
                FONT_SIZE_MEDIUM, fieldAnchor = SVGConstants.SVG_MIDDLE_VALUE,
                fieldOffset = inner.width * 0.5, parent = g)
        } else {
            addFieldSet(
                listOf(
                    LabeledField(bundle.getString("nose"), "noseHeat", "0"),
                    LabeledField(bundle.getString("foreSides"), "foreSidesHeat", "0/0"),
                    LabeledField(bundle.getString("aftSides"), "aftSidesHeat", "0/0"),
                    LabeledField(bundle.getString("aft"), "aftHeat", "0")
                ), inner.x + inner.width * 0.35, inner.y + col1Height + lineHeight,
                FONT_SIZE_MEDIUM, fieldAnchor = SVGConstants.SVG_MIDDLE_VALUE,
                fieldOffset = inner.width * 0.45, parent = g)
        }
        rootElement.appendChild(g)
    }

    private fun addBombsPanel() {
        val label = RSLabel(this, 0.0, 0.0,
            bundle.getString("bombsPanel.title"), FONT_SIZE_FREE_LABEL)
        val g = createTranslatedGroup(width() - label.rectWidth, 0.0)
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "external_stores")
        g.appendChild(label.draw())
        addRect(label.rectMargin, label.height() + padding, label.rectWidth, label.rectWidth * 0.8,
            id = "bomb_boxes", parent = g)
        val keyGroup = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        keyGroup.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "external_stores_key")
        val lineHeight = calcFontHeight(FONT_SIZE_VSMALL).toDouble()
        var ypos = lineHeight
        addTextElement(label.rectWidth * 0.5, ypos, bundle.getString("key"),
            fontSize = FONT_SIZE_VSMALL, fontWeight = SVGConstants.SVG_BOLD_VALUE, parent = keyGroup)
        ypos += lineHeight
        addTextElement(label.rectWidth * 0.5, ypos, bundle.getString("highExplosive"), fontSize = FONT_SIZE_VSMALL, parent = keyGroup)
        ypos += lineHeight
        addTextElement(label.rectWidth * 0.5, ypos, bundle.getString("laser"), fontSize = FONT_SIZE_VSMALL, parent = keyGroup)
        ypos += lineHeight
        addTextElement(label.rectWidth * 0.5, ypos, bundle.getString("cluster"), fontSize = FONT_SIZE_VSMALL, parent = keyGroup)
        ypos += lineHeight
        addTextElement(label.rectWidth * 0.5, ypos, bundle.getString("rocket"), fontSize = FONT_SIZE_VSMALL, parent = keyGroup)
        g.appendChild(keyGroup)
        rootElement.appendChild(g)
    }

    private fun addGroundMovementTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("groundMovementTable.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / 20
        var ypos = inner.y + lineHeight
        addTextElement(inner.width * 0.5, ypos, bundle.getString("groundMovementTable.minStraightMovement"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        addTextElement(inner.width * 0.5, ypos, bundle.getString("groundMovementTable.inHexes"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight * 1.2
        addTextElement(inner.width * 0.75, ypos, bundle.getString("groundMovementTable.smallCraftFixed"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            listOf(
                listOf("1", "8", "8"),
                listOf("2", "12", "14"),
                listOf("3", "16", "20"),
                listOf("4", "20", "26"),
                listOf("5", "24", "32"),
                listOf("6", "28", "38"),
                listOf("7", "32", "44"),
                listOf("8", "36", "50"),
                listOf("9", "40", "56"),
                listOf("10", "44", "62"),
                listOf("11", "48", "68"),
                listOf("12", "52", "74")
            ), listOf(0.15, 0.4, 0.75),
            listOf(bundle.getString("groundMovementTable.velocity"),
                bundle.getString("groundMovementTable.fighter"),
                bundle.getString("groundMovementTable.wingSupportVehicle")),
            lineHeight = lineHeight, parent = g)
        addTextElement(inner.width * 0.5, ypos + lineHeight, bundle.getString("groundMovementTable.footnote"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_NORMAL_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        rootElement.appendChild(g)
    }

    private fun addFighterReturnTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("fighterReturnTable.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / 7
        var ypos = inner.y + lineHeight * 1.5
        ypos += createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            listOf(
                listOf("1-4", "3"),
                listOf("5-8", "2"),
                listOf("9-12", "1"),
                listOf("13+", "0")
            ), listOf(0.23, 0.7),
            listOf(bundle.getString("fighterReturnTable.safeThrust"),
                bundle.getString("fighterReturnTable.turnsBeforeReturn")),
            lineHeight = lineHeight, parent = g)
        rootElement.appendChild(g)
    }
}

class ASFRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override fun colorElements() = "${super.colorElements()},heatScale"
    override val fileName = "fighter_aerospace_default.svg"
    override val armorDiagramFileName = "armor_diagram_asf.svg"
    override val dataPanelTitle: String = bundle.getString("fighterData")
    override val fighter = true
    override val tracksHeat = true
    override val largeCraft = false
    override fun isAerodyne() = true
}

class ConvFighterRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "fighter_conventional_default.svg"
    override val armorDiagramFileName = "armor_diagram_convfighter.svg"
    override val dataPanelTitle: String = bundle.getString("fighterData")
    override val fighter = true
    override val tracksHeat = false
    override val largeCraft = false
    override fun isAerodyne() = true
    override fun isAtmospheric() = true
}

class AerodyneSmallCraftRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override fun colorElements() = "${super.colorElements()},heatScale"
    override val fileName = "smallcraft_aerodyne_default.svg"
    override val armorDiagramFileName = "armor_diagram_smallcraft_aerodyne.svg"
    override val dataPanelTitle: String = bundle.getString("craftData")
    override val fighter = false
    override val tracksHeat = true
    override val largeCraft = false
    override fun isAerodyne() = true
}

class SpheroidSmallCraftRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override fun colorElements() = "${super.colorElements()},heatScale"
    override val fileName = "smallcraft_spheroid_default.svg"
    override val armorDiagramFileName = "armor_diagram_smallcraft_spheroid.svg"
    override val dataPanelTitle: String = bundle.getString("craftData")
    override val fighter = false
    override val tracksHeat = true
    override val largeCraft = false
}

class AerodyneDropshipRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "dropship_aerodyne_default.svg"
    override val armorDiagramFileName = "armor_diagram_dropship_aerodyne.svg"
    override val dataPanelTitle: String = bundle.getString("dropshipData")
    override val fighter = false
    override val tracksHeat = false
    override val largeCraft = true
    override fun isAerodyne() = true
}

class SpheroidDropshipRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "dropship_spheroid_default.svg"
    override val armorDiagramFileName = "armor_diagram_dropship_spheroid.svg"
    override val dataPanelTitle: String = bundle.getString("dropshipData")
    override val fighter = false
    override val tracksHeat = false
    override val largeCraft = true
}

class JumpshipRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "jumpship_default.svg"
    override val armorDiagramFileName = "armor_diagram_jumpship.svg"
    override val dataPanelTitle: String = bundle.getString("jumpshipData")
    override val fighter = false
    override val tracksHeat = false
    override val largeCraft = true
    override fun isCapitalScale() = true
    override fun stationKeeping() = true
}

class WarshipRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "warship_default.svg"
    override val armorDiagramFileName = "armor_diagram_warship.svg"
    override val dataPanelTitle: String = bundle.getString("warshipData")
    override val fighter = false
    override val tracksHeat = false
    override val largeCraft = true
    override fun isCapitalScale() = true
}

class SpaceStationRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "spacestation_default.svg"
    override val armorDiagramFileName = "armor_diagram_spacestation.svg"
    override val dataPanelTitle: String = bundle.getString("stationData")
    override val fighter = false
    override val tracksHeat = false
    override val largeCraft = true
    override fun isCapitalScale() = true
    override fun stationKeeping() = true
    override fun isStation() = true
}

class LargeCraftPageTwo(size: PaperSize): RecordSheet(size) {
    override val fileName = "advaero_reverse.svg"

    private val bundle: ResourceBundle = ResourceBundle.getBundle(AeroRecordSheet::class.java.name)
    private val compassCell = Cell(width() * 2.0 / 3.0 + padding, padding,
        width() / 3.0 - padding * 2,logoHeight + titleHeight - padding)
    private val eqTableCell = Cell(0.0, logoHeight + titleHeight + padding,
        width() * 0.5 - padding,height() - logoHeight - titleHeight - footerHeight - padding)
    private val advMovementCell = Cell(eqTableCell.rightX() + padding, eqTableCell.y,
        width() * 0.5,eqTableCell.height * 0.5)
    private val velocityRecordCell = Cell(advMovementCell.x, advMovementCell.bottomY() + padding,
        advMovementCell.width, advMovementCell.height - padding * 2)

    override fun build() {
        addCompassCell(compassCell)
        addEquipmentTable(eqTableCell)
        addAdvMovementTable(advMovementCell)
        addVelocityRecordCell(velocityRecordCell)
    }

    private fun addCompassCell(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val widthBelow = calcTextLength(bundle.getString("advMovementTable.title"), FONT_SIZE_TAB_LABEL) +
                4 + tabBevelX * 2 + padding - width() / 6.0
        val shadow = CellBorder(2.5, 2.5, rect.width - 6.0, rect.height - 6.0,
            0.0, FILL_LIGHT_GREY, 5.2, topTab = false, bevelTopLeft = true, bevelTopRight = true,
            bottomTab = true, labelWidthBelow = widthBelow)
        val border = CellBorder(0.0, 0.0, rect.width - 5.0, rect.height - 5.0,
            0.0, topTab = false, bevelTopLeft = true, bevelTopRight = true,
            bottomTab = true, labelWidthBelow = widthBelow)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        addAeroMovementCompass(Cell(0.0, 0.0 + tabBevelY, rect.width - 5.0,
            rect.height - 5.0 - padding), FONT_SIZE_VLARGE, g)
        rootElement.appendChild(g)

    }

    private fun addEquipmentTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unitDataPanel")
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            "%s DATA (Cont.)", topTab = true, bottomTab = false, bevelTopRight = true,
            bevelBottomLeft = true, bevelBottomRight = true, parent = g)
        var ypos = internal.y
        val fontSize = 9.67f
        val lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x + padding,
            ypos, fontSize, maxWidth = internal.width - internal.x - padding, parent = g)
        ypos += lineHeight
        addField(bundle.getString("name"), "fluffName", internal.x, ypos,
            fontSize, blankId = "blankFluffName",
            blankWidth = internal.width * 0.45 - calcTextLength(bundle.getString("name") + "_", fontSize),
            parent = g)
        ypos += lineHeight
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, bundle.getString("weaponsAndEquipment"),
            FONT_SIZE_FREE_LABEL,
            SVGConstants.SVG_BOLD_VALUE, fixedWidth = true, width = internal.width * 0.6, parent = g)

        addRect(internal.x, ypos, internal.width - padding, internal.bottomY() - ypos,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g)

        rootElement.appendChild(g)
    }

    private fun addAdvMovementTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            bundle.getString("advMovementTable.title"), topTab = true, bottomTab = false, bevelTopRight = true,
            bevelBottomLeft = true, bevelBottomRight = true, parent = g)
        var ypos = internal.y
        val fontSize = FONT_SIZE_FREE_LABEL
        val indent = calcTextLength("_", fontSize) * 3
        val text = (1..4).map{bundle.getString("advMovementTable.$it")}.toList()
        val lines = text.map{it.split("\n".toRegex()).size}.sum()
        val lineHeight = internal.height / 2.0 / (lines + 2)
        for (i in 0..3) {
            ypos += addParagraph(internal.x + padding, ypos, internal.width - padding * 2, text[i],
                fontSize, lineHeight, indent, parent = g)
        }
        ypos += lineHeight * 3
        addTextElement(internal.x + padding, ypos, bundle.getString("advMovementTable.opposingVectors.title"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += addParagraph(internal.x + padding, ypos, internal.width * 0.45 - padding * 2,
            bundle.getString("advMovementTable.opposingVectors.text"), fontSize, lineHeight, indent, parent = g)
        ypos += lineHeight * 3
        addTextElement(internal.x + padding, ypos, bundle.getString("advMovementTable.obliqueVectors.title"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += addParagraph(internal.x + padding, ypos, internal.width * 0.45 - padding * 2,
            bundle.getString("advMovementTable.obliqueVectors.text"), fontSize, lineHeight, indent, parent = g)

        embedImage(internal.x + internal.width * 0.45, internal.y + internal.height * 0.5 - lineHeight * 2,
            internal.width * 0.5 - padding, internal.height * 0.5, "aero_vector_diagram.svg",
            ImageAnchor.CENTER, parent = g)
        embedImage(internal.x + padding, internal.bottomY() - padding * 2 - CGL_LOGO_HEIGHT,
            CGL_LOGO_WIDTH, CGL_LOGO_HEIGHT, CGL_LOGO, CGL_LOGO_BW, anchor = ImageAnchor.BOTTOM_RIGHT, parent = g)

        rootElement.appendChild(g)
    }

    private fun addVelocityRecordCell(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("velocityRecord.title"), topTab = true, bottomTab = false,
            parent = g)
        val fontSize = FONT_SIZE_LARGE
        val lineHeight = inner.height / 23.0
        var ypos = inner.y + lineHeight
        val colX = listOf(0.04, 0.1, 0.25, 0.42, 0.5, 0.58, 0.66, 0.74, 0.82, 0.88).map {
            inner.x + it * inner.width
        }.toList()
        addTextElement(colX[0], ypos, bundle.getString("advancedVelTable.turn"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[5], ypos, bundle.getString("advancedVelTable.velocity"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        addTextElement(colX[0], ypos, "#", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[1], ypos, bundle.getString("advancedVelTable.thrust"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, parent = g)
        addTextElement(colX[2], ypos, bundle.getString("advancedVelTable.facing"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, parent = g)
        addTextElement(colX[3], ypos, "A", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[4], ypos, "B", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[5], ypos, "C", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[6], ypos, "D", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[7], ypos, "E", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[8], ypos, "F", fontSize,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(colX[9], ypos, bundle.getString("advancedVelTable.fuel"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += lineHeight
        for (row in 1..20) {
            addTextElement(colX[0], ypos, row.toString(), fontSize,
                SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
            addHorizontalLine(colX[1], ypos + 1.0, width = inner.width * 0.1, strokeWidth = 0.72, parent = g)
            addHorizontalLine(colX[2], ypos + 1.0, width = inner.width * 0.1, strokeWidth = 0.72, parent = g)
            addHorizontalLine(colX[9], ypos + 1.0, width = inner.width * 0.1, strokeWidth = 0.72, parent = g)
            for (col in 3..8) {
                addTextElement(colX[col], ypos, "/", fontSize, SVGConstants.SVG_BOLD_VALUE,
                    anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
                addHorizontalLine(colX[col] - inner.width * 0.035, ypos + 1.0, width = inner.width * 0.025,
                    strokeWidth = 0.72, parent = g)
                addHorizontalLine(colX[col], ypos + 1.0, width = inner.width * 0.025,
                    strokeWidth = 0.72, parent = g)
            }
            ypos += lineHeight
        }
        rootElement.appendChild(g)
    }
}
