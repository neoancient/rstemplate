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
    val crewCell = Cell(eqTableCell.rightX(), eqTableCell.y,
        width() - eqTableCell.width - armorCell.width, eqTableCell.height / 3.0 - padding)
    val criticalDamageCell = Cell(crewCell.x, crewCell.bottomY() + padding, crewCell.width, crewCell.height)
    val notesCell = Cell(crewCell.x, criticalDamageCell.bottomY() + padding, crewCell.width, crewCell.height)

    protected val bundle = ResourceBundle.getBundle(VehicleRecordSheet::class.java.name)
    abstract val turretCount: Int
    abstract val armorDiagramFileName: String
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
            LabeledField(bundle.getString("flanking"), "mpRun", "0")
        ), internal.x + padding, ypos + lineHeight, fontSize, FILL_DARK_GREY, 38.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addField(bundle.getString("jumping"), "mpJump",
            internal.x + internal.width * 0.25, ypos + lineHeight * 2, fontSize,
            FILL_DARK_GREY, "0", 38.0, labelId = "lblJump", parent = g)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), internal.x + padding + internal.width * 0.5, ypos, fontSize, FILL_DARK_GREY, parent = g)
        ypos += lineHeight * 3
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
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = inner.y + lineHeight * 1.5
        addField(bundle.getString("crew"), "crewName", padding, ypos, fontSize,
            blankId = "blankCrewName",
            blankWidth = inner.width - padding * 2
                    - calcTextLength("${bundle.getString("crew")}_", fontSize, fontWeight),
            labelFixedWidth = false, parent = g)
        ypos += lineHeight * 1.5
        addField(bundle.getString("gunnerySkill"), "gunnerySkill", padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankGunnerySkill", labelId = "gunnerySkillText",
            blankWidth = inner.width * 0.13, parent = g)
        addField(bundle.getString("drivingSkill"), "drivingSkill", inner.width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankDrivingSkill", labelId = "drivingSkillText",
            blankWidth = inner.width * 0.18 - padding, parent = g)
        ypos += lineHeight * 2.0
        g.appendChild(DamageCheckBox(bundle.getString("commanderHit"), "+1")
            .draw(this, inner.x, ypos, fontSize, width = inner.width * 0.45))
        g.appendChild(DamageCheckBox(bundle.getString("driverHit"), "+2")
            .draw(this, inner.x + inner.width * 0.5, ypos, fontSize, width = inner.width * 0.45))
        ypos += lineHeight * 1.8
        addTextElement(inner.x, ypos, bundle.getString("commanderHitMod"),
            4.83f, fixedWidth = true, parent = g)
        addTextElement(inner.x + inner.width * 0.5, ypos, bundle.getString("driverHitMod"),
            4.83f, fixedWidth = true, parent = g)
        document.documentElement.appendChild(g)
    }

    fun addCriticalDamagePanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("criticalDamage.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = inner.height / 7.0
        var ypos = inner.y + lineHeight * 0.5
        if (turretCount == 1) {
            g.appendChild(DamageCheckBox(bundle.getString("turretLocked"))
                    .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.55))
        } else if (turretCount == 2) {
            g.appendChild(DamageCheckBox(bundle.getString("turretLocked"), listOf("F", "R"))
                .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.55))
        }
        g.appendChild(DamageCheckBox(bundle.getString("engineHit"))
            .draw(this, if (turretCount == 0) inner.x + padding else inner.x + inner.width * 0.6, ypos,
                fontSize, width = inner.width * 0.35))
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("sensorHits"), listOf("+1", "+2", "+3", "D"))
            .draw(this, inner.x + padding, ypos, fontSize,
                offset = inner.width * 0.95 - padding - (calcFontHeight(fontSize) + padding) * 4))
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("motiveSystemHits"), listOf("+1", "+2", "+3"))
            .draw(this, inner.x + padding, ypos, fontSize,
                offset = inner.width * 0.95 - padding - (calcFontHeight(fontSize) + padding) * 4))
        ypos += lineHeight
        addTextElement(inner.x + inner.width * 0.5, ypos + lineHeight * 0.5, bundle.getString("stabilizers"),
            fontSize, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("front"))
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.29))
        g.appendChild(DamageCheckBox(bundle.getString("left"))
            .draw(this, inner.x + inner.width * 0.33, ypos, fontSize, width = inner.width * 0.29))
        g.appendChild(DamageCheckBox(bundle.getString("right"))
            .draw(this, inner.x + padding + inner.width * 0.6315, ypos, fontSize, width = inner.width * 0.29))
        ypos += lineHeight
        g.appendChild(DamageCheckBox(bundle.getString("rear"))
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.29))
        if (turretCount == 1) {
            g.appendChild(DamageCheckBox(bundle.getString("turret"))
                .draw(this, inner.x + inner.width * 0.33, ypos, fontSize, width = inner.width * 0.29))
        } else if (turretCount == 2) {
            g.appendChild(DamageCheckBox(bundle.getString("turret1"))
                .draw(this, inner.x + inner.width * 0.33, ypos, fontSize, width = inner.width * 0.29))
            g.appendChild(DamageCheckBox(bundle.getString("turret2"))
                .draw(this, inner.x + padding + inner.width * 0.6315, ypos, fontSize, width = inner.width * 0.29))
        }
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
        embedImage(0.0, 0.0, rect.width, rect.height, armorDiagramFileName, ImageAnchor.CENTER, g)
        embedImage(rect.width - 50.0, rect.height - 30.0, 50.0, 30.0,
            CGL_LOGO, anchor = ImageAnchor.BOTTOM_RIGHT, parent = g)
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
    }
}

class SingleTurretVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_turret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vee_turret.svg"
    override val turretCount = 1
}

class NoTurretVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_noturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vee_noturret.svg"
    override val turretCount = 0
}

class DualTurretVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_dualturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vee_dualturret.svg"
    override val turretCount = 2
}