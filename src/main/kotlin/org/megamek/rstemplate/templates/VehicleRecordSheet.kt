package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
abstract class VehicleRecordSheet(size: PaperSize): RecordSheet(size) {
    val eqTableCell = if (fullPage()) {
        Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight,
            width() * 0.4, (height() - footerHeight) / 2.0 - logoHeight - titleHeight)
    } else {
        Cell(LEFT_MARGIN.toDouble(), logoHeight + titleHeight,
            width() * 0.4, height() - footerHeight - logoHeight - titleHeight - padding)
    }
    val armorCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0 + padding,
            if (fullPage()) TOP_MARGIN.toDouble() else padding,
            width() / 3.0 - padding,height() - footerHeight - padding * 2.0)
    val crewCell = Cell(eqTableCell.rightX(), eqTableCell.y,
        width() - eqTableCell.width - armorCell.width, eqTableCell.height / 3.0 - padding
    )
    val criticalDamageCell = Cell(crewCell.x, crewCell.bottomY() + padding, crewCell.width, crewCell.height)
    val notesCell = Cell(crewCell.x, criticalDamageCell.bottomY() + padding, crewCell.width, crewCell.height + padding)

    protected val bundle = ResourceBundle.getBundle(VehicleRecordSheet::class.java.name)
    abstract val turretCount: Int
    abstract val armorDiagramFileName: String
    open fun isVTOL() = false

    final override fun height(): Double = if (!fullPage()) {
        size.height * 0.5 - TOP_MARGIN - padding
    } else {
        super.height()
    }

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
            bundle.getString("dataPanel.title"), true,
            true, false, parent = g)
        var ypos = internal.y
        var fontSize = 9.67f
        var lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x, ypos, fontSize,
            SVGConstants.SVG_BOLD_VALUE, maxWidth = internal.width, parent = g)
        ypos += lineHeight
        fontSize = 7.7f
        lineHeight = calcFontHeight(fontSize)
        addTextElement(internal.x + padding, ypos, bundle.getString("movementPoints"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = g)
        addFieldSet(listOf(
            LabeledField(bundle.getString("cruising"), "mpWalk", "0"),
            LabeledField(bundle.getString("flanking"), "mpRun", "0")
        ), internal.x + padding, ypos + lineHeight, fontSize,
            FILL_DARK_GREY, 40.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addField(bundle.getString("jumping"), "mpJump",
            internal.x + internal.width * 0.25, ypos + lineHeight * 2, fontSize,
            FILL_DARK_GREY, "0", 38.0, labelId = "lblJump", parent = g)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), internal.x + padding + internal.width * 0.5, ypos, fontSize,
            FILL_DARK_GREY, parent = g)
        ypos += lineHeight * 3
        addFieldSet(listOf(
            LabeledField(bundle.getString("movementType"), "movementType"),
            LabeledField(bundle.getString("engineType"), "engineType")
        ), internal.x + padding, ypos, fontSize,
            FILL_DARK_GREY, parent = g)
        ypos += lineHeight * 2
        addHorizontalLine(internal.x, ypos - lineHeight * 0.5, internal.width - padding, parent = g)
        ypos += lineHeight * 0.5
        addTextElement(internal.x, ypos, bundle.getString("weaponsAndEquipment"),
            FONT_SIZE_FREE_LABEL, SVGConstants.SVG_BOLD_VALUE, fixedWidth = true,
            width = internal.width * 0.6, parent = g)
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

    fun addCrewPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("crewPanel.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val fontWeight = SVGConstants.SVG_BOLD_VALUE
        val lineHeight = calcFontHeight(fontSize)
        var ypos = inner.y + lineHeight * 1.5
        addField(bundle.getString("crew"), "pilotName0",
            padding, ypos, fontSize,
            blankId = "blankCrewName0",
            blankWidth = inner.width - padding * 2
                    - calcTextLength("${bundle.getString("crew")}_",
                fontSize, fontWeight),
            labelFixedWidth = false, parent = g)
        ypos += lineHeight * 1.5
        addField(bundle.getString("gunnerySkill"), "gunnerySkill0",
            padding,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankGunnerySkill0", labelId = "gunnerySkillText0",
            blankWidth = inner.width * 0.13, parent = g)
        addField(bundle.getString("drivingSkill"), "pilotingSkill0", inner.width * 0.5,
            ypos, fontSize, defaultText = "0",
            fieldOffset = inner.width * 0.32,
            blankId = "blankPilotingSkill0", labelId = "pilotingSkillText0",
            blankWidth = inner.width * 0.18 - padding, parent = g)
        ypos += lineHeight * 2.0
        g.appendChild(DamageCheckBox(bundle.getString(if (isVTOL()) "copilotHit" else "commanderHit"), "+1")
            .draw(this, inner.x, ypos, fontSize, width = inner.width * 0.45))
        g.appendChild(DamageCheckBox(bundle.getString(if (isVTOL()) "pilotHit" else "driverHit"), "+2")
            .draw(this, inner.x + inner.width * 0.5, ypos, fontSize, width = inner.width * 0.45))
        ypos += lineHeight * 1.8
        addTextElement(inner.x, ypos, bundle.getString("commanderHitMod"),
            4.83f, fixedWidth = true, parent = g)
        addTextElement(inner.x + inner.width * 0.5, ypos, bundle.getString("driverHitMod"),
            4.83f, fixedWidth = true, parent = g)
        document.documentElement.appendChild(g)
    }

    open fun addCriticalDamagePanel(rect: Cell) {
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
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "notes")
        addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("notes.title"), parent = g)
        document.documentElement.appendChild(g)
        val fluffCell = rect.inset(
            padding,
            padding,
            padding,
            padding
        )
        addRect(fluffCell.x, fluffCell.y, fluffCell.width, fluffCell.height,
            stroke = SVGConstants.SVG_NONE_VALUE, id = "fluffImage")
    }

    open fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(this, rect.width * 0.5, 0.0, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        embedImage(0.0, label.height(), rect.width, rect.height - label.height() - padding,
            armorDiagramFileName, ImageAnchor.CENTER, g)
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
    override fun fullPage() = false
}

class NoTurretVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_noturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vee_noturret.svg"
    override val turretCount = 0
    override fun fullPage() = false
}

class DualTurretVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_dualturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vee_dualturret.svg"
    override val turretCount = 2
    override fun fullPage() = false
}

class SingleTurretSHVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_turret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_vee_turret.svg"
    override val turretCount = 1
    override fun fullPage() = false
}

class NoTurretSHVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_noturret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_vee_noturret.svg"
    override val turretCount = 0
    override fun fullPage() = false
}

class DualTurretSHVehicleRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override val fileName = "vehicle_dualturret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_vee_dualturret.svg"
    override val turretCount = 2
    override fun fullPage() = false
}

abstract class AbstractVTOLRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override fun fullPage() = false
    override fun isVTOL() = true
    override fun addCriticalDamagePanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("criticalDamage.title"), parent = g)
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = inner.height / (6.0 + turretCount)
        var ypos = inner.y + lineHeight * 0.5
        g.appendChild(DamageCheckBox(bundle.getString("flightStabilizer"), listOf("+3"))
            .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.55))
        g.appendChild(DamageCheckBox(bundle.getString("engineHit"))
            .draw(this, inner.x + inner.width * 0.6, ypos,
                fontSize, width = inner.width * 0.35))
        ypos += lineHeight
        if (turretCount == 1) {
            g.appendChild(DamageCheckBox(bundle.getString("turretLocked"))
                .draw(this, inner.x + padding, ypos, fontSize, width = inner.width * 0.55))
            ypos += lineHeight
        }
        g.appendChild(DamageCheckBox(bundle.getString("sensorHits"), listOf("+1", "+2", "+3", "D"))
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
        }
        ypos += lineHeight
        if (isVTOL()) {
            addTextElement(inner.x + padding, ypos + padding, bundle.getString("cruisingOnly"),
                4.83f, fixedWidth = true, parent = g)
        }
        document.documentElement.appendChild(g)
    }
}

class VTOLRecordSheet(size: PaperSize): AbstractVTOLRecordSheet(size) {
    override val fileName = "vtol_noturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vtol_noturret.svg"
    override val turretCount = 0
}

class VTOLTurretRecordSheet(size: PaperSize): AbstractVTOLRecordSheet(size) {
    override val fileName = "vtol_turret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_vtol_chinturret.svg"
    override val turretCount = 1
}

abstract class BaseNavalRecordSheet(size: PaperSize): VehicleRecordSheet(size) {
    override fun fullPage() = true
    open fun isSubmarine() = false

    val hitLocationCell = Cell(eqTableCell.x, eqTableCell.bottomY() + padding * 2,
        eqTableCell.width, (height() - footerHeight) * 0.3 - padding)
    val motiveTableCell = Cell(notesCell.x, hitLocationCell.y,
        notesCell.width, hitLocationCell.height)
    val criticalHitsCell = Cell(hitLocationCell.x, hitLocationCell.bottomY() + padding,
        motiveTableCell.rightX() - hitLocationCell.x, (height() - footerHeight) * 0.2 - padding)

    override fun build() {
        super.build()
        NavalHitLocationTable(this).draw(hitLocationCell)
        NavalMotiveDamageTable(this).draw(motiveTableCell)
        NavalCriticalHitTable(this).draw(criticalHitsCell)
    }

    override fun addArmorDiagram(rect: Cell) {
        if (isSubmarine()) {
            val newArmorPanel = Cell(rect.x, rect.y, rect.width, rect.height * 0.87)
            super.addArmorDiagram(newArmorPanel)
            val g = createTranslatedGroup(rect.x + padding, newArmorPanel.bottomY())
            val inner = addBorder(0.0, 0.0, rect.width - padding,
                criticalHitsCell.bottomY() - newArmorPanel.bottomY(),
                bundle.getString("depthTable.title"), true, false,
                parent = g)
            addDepthTrack(padding * 1.5, inner.y + inner.height * 0.1,
                inner.width - padding * 2, inner.height * 0.3, (1..10).toList(), g)
            addDepthTrack(padding * 1.5, inner.y + inner.height * 0.55,
                inner.width - padding * 2, inner.height * 0.3, (11..20).toList(), g)
            document.documentElement.appendChild(g)
        } else {
            super.addArmorDiagram(rect)
        }
    }

    private fun addDepthTrack(x: Double, y: Double,
                              width: Double, height: Double, range: List<Int>, parent: Element) {
        val outline = RoundedBorder(x, y, width, height,
            1.315, 0.726, 1.0).draw(document)
        parent.appendChild(outline)
        val grid = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        val colOffset = x + width * 0.2
        val colWidth = (width - colOffset) / range.size
        grid.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${x.truncate()},${(y + height / 2.0).truncate()}"
                    + " h${width.truncate()}"
                    + (0 until range.size).map {
                " M ${(colOffset + it * colWidth).truncate()},${y.truncate()} v${height.truncate()}"
            }.joinToString(" "))
        grid.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        grid.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE,
            FILL_DARK_GREY)
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "0.58")
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        parent.appendChild(grid)
        val fontSize = FONT_SIZE_MEDIUM
        val startX = colOffset + colWidth * 0.5
        val startY = y + (height - calcFontHeight(fontSize)) * 0.5 - 1
        addTextElement(x + padding, startY, bundle.getString("turn"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addTextElement(x + padding, height / 2.0 + startY, bundle.getString("depth"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        for (i in 0 until range.size) {
            addTextElement(startX + i * colWidth, startY, range[i].toString(), fontSize,
                SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
                parent = parent)
        }
    }
}

class NavalTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "naval_turret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_naval_turret.svg"
    override val turretCount = 1
}

class NavalNoTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "naval_noturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_naval_noturret.svg"
    override val turretCount = 0
}

class NavalDualTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "naval_dualturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_naval_dualturret.svg"
    override val turretCount = 2
}

class SHNavalNoTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "naval_noturret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_naval_noturret.svg"
    override val turretCount = 0
}

class SHNavalTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "naval_turret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_naval_turret.svg"
    override val turretCount = 1
}

class SHNavalDualTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "naval_dualturret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_naval_dualturret.svg"
    override val turretCount = 2
}

class SubmarineTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "submarine_turret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_naval_turret.svg"
    override val turretCount = 1
    override fun isSubmarine() = true
}

class SubmarineNoTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "submarine_noturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_naval_noturret.svg"
    override val turretCount = 0
    override fun isSubmarine() = true
}

class SubmarineDualTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "submarine_dualturret_standard.svg"
    override val armorDiagramFileName = "armor_diagram_naval_dualturret.svg"
    override val turretCount = 2
    override fun isSubmarine() = true
}

class SHSubmarineNoTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "submarine_noturret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_naval_noturret.svg"
    override val turretCount = 0
    override fun isSubmarine() = true
}

class SHSubmarineTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "submarine_turret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_naval_turret.svg"
    override val turretCount = 1
    override fun isSubmarine() = true
}

class SHSubmarineDualTurretRecordSheet(size: PaperSize): BaseNavalRecordSheet(size) {
    override val fileName = "submarine_dualturret_superheavy.svg"
    override val armorDiagramFileName = "armor_diagram_sh_naval_dualturret.svg"
    override val turretCount = 2
    override fun isSubmarine() = true
}

