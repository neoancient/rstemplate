package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 * Base class for aerospace record sheets
 */
abstract class AeroRecordSheet(size: PaperSize): RecordSheet(size) {

    val eqTableCell = Cell(
        LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + titleHeight,
        width() * 0.4, height() * 0.5 - logoHeight - titleHeight)
    val armorCell = Cell(eqTableCell.rightX() + padding, TOP_MARGIN.toDouble(),
        width() * 0.6 - padding, height() * 0.65 - padding)
    val fluffCell = Cell(LEFT_MARGIN.toDouble(), eqTableCell.bottomY() + padding,
        eqTableCell.width, armorCell.bottomY() - eqTableCell.bottomY() - padding)
    val tableCell = Cell(LEFT_MARGIN.toDouble(), armorCell.bottomY() + padding,
        width().toDouble(), height() * 0.35 - footerHeight - padding)
    val critDamageCell = Cell(LEFT_MARGIN.toDouble(), fluffCell.bottomY() + padding,
        eqTableCell.width, height() * 0.12)
    val velocityCell = Cell(LEFT_MARGIN.toDouble(), critDamageCell.bottomY() + padding,
        width() * 2.0 / 3.0, height() - critDamageCell.bottomY() - footerHeight)
    val pilotCell = Cell(critDamageCell.rightX() + padding, critDamageCell.y,
        velocityCell.width - critDamageCell.width - padding, critDamageCell.height)
    val heatScaleCell = Cell(armorCell.rightX() - 20, TOP_MARGIN + (height() - footerHeight) / 2.0,
        20.0, (height() - footerHeight) / 2.0)
    val heatCell = Cell(velocityCell.rightX() + padding, tableCell.y,
        width() - velocityCell.width - heatScaleCell.width - padding, height() - armorCell.height - footerHeight - padding)

    protected val bundle = ResourceBundle.getBundle(AeroRecordSheet::class.java.name)

    final override fun height() = super.height()
    abstract val dataPanelTitle: String
    abstract val armorDiagramFileName: String

    override fun build() {
        addEquipmentTable(eqTableCell)
        addFluffPanel(fluffCell)
        addArmorDiagram(armorCell)
        addCritPanel(critDamageCell)
        addPilotPanel(pilotCell)
        addHeatPanel(heatCell)
        addHeatScale(heatScaleCell)
    }

    fun addEquipmentTable(rect: Cell) {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${rect.x.truncate()},${rect.y.truncate()})")
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unitDataPanel")
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height - padding,
            dataPanelTitle, true,true,
            false, parent = g)
        var ypos = internal.y
        val fontSize = 9.67f
        val lineHeight = calcFontHeight(fontSize)
        ypos += lineHeight
        addField(bundle.getString("type"), "type", internal.x, ypos, fontSize, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = internal.width - internal.x - padding, parent = g)
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
        document.documentElement.appendChild(g)
    }

    fun addFluffPanel(rect: Cell) {
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

    /**
     * Adds the fields and labels at the top of the unit data panel
     */
    open fun addUnitDataFields(x: Double, y: Double, width: Double, parent: Element): Double {
        val fontSize = 7.7f
        val lineHeight = calcFontHeight(fontSize).toDouble()
        addTextElement(x, y, bundle.getString("thrust"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("safeThrust"), "mpWalk", "0"),
            LabeledField(bundle.getString("maxThrust"), "mpRun", "0")
        ), x, y + lineHeight, fontSize, FILL_DARK_GREY, 70.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        addFieldSet(listOf(
            LabeledField(bundle.getString("tonnage"), "tonnage", "0"),
            LabeledField(bundle.getString("techBase"), "techBase","Inner Sphere"),
            LabeledField(bundle.getString("rulesLevel"), "rulesLevel","Standard"),
            LabeledField(bundle.getString("role"), "role", labelId = "labelRole")
        ), x + width * 0.5, y, fontSize, FILL_DARK_GREY, maxWidth = width * 0.5 - padding, parent = parent)
        return lineHeight * 4
    }

    fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(this, 0.0, logoHeight + titleHeight,
            bundle.getString("armorPanel.title"), FONT_SIZE_FREE_LABEL)
        g.appendChild(label.draw())
        embedImage(0.0, logoHeight - 20, rect.width - heatScaleCell.width,
            rect.height - logoHeight + 20,
            armorDiagramFileName, ImageAnchor.CENTER, g)
        embedImage(rect.width - 50.0 - heatScaleCell.width - padding, rect.height - 30.0, 50.0, 30.0,
            CGL_LOGO, anchor = ImageAnchor.BOTTOM_RIGHT, parent = g)
        document.documentElement.appendChild(g)
    }

    fun addCritPanel(rect: Cell) {

    }

    fun addPilotPanel(rect: Cell) {

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
}

class ASFRecordSheet(size: PaperSize): AeroRecordSheet(size) {
    override val fileName = "fighter_aerospace_default.svg"
    override val armorDiagramFileName = "armor_diagram_asf.svg"
    override val dataPanelTitle = bundle.getString("fighterData")

}