package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
abstract class MultiUnitSheet(size: PaperSize): RecordSheet(size) {

    private val titleCell = Cell(width() * 2.0 / 3.0 + padding, padding,
        width() / 3.0 - padding, logoHeight)
    private val unitCell = Cell(0.0, logoHeight + padding * 2,
        width(), height() - logoHeight - footerHeight - padding * 2)
    override fun showTitle() = false
    final override fun height() = super.height()

    protected val bundle: ResourceBundle = ResourceBundle.getBundle(MultiUnitSheet::class.java.name)
    abstract val unitCount: Int
    abstract val unitCapacity: Int
    abstract val encodedFluffImage: String
    abstract val title: List<String>
    abstract fun logoInFooter(): Boolean

    override fun build() {
        addTitleCell(titleCell)
        addUnitGroups(unitCell)
    }

    override fun addCopyrightFooter(x: Double, width: Double, parent: Element): Double {
        return if (logoInFooter()) {
            super.addCopyrightFooter(CGL_LOGO_WIDTH + padding, width() - CGL_LOGO_WIDTH - padding, parent)
            embedImage(0.0, height() - CGL_LOGO_HEIGHT, CGL_LOGO_WIDTH, CGL_LOGO_HEIGHT,
                CGL_LOGO, CGL_LOGO_BW, anchor = ImageAnchor.BOTTOM_LEFT, id = "cglLogo", parent = parent)
            CGL_LOGO_HEIGHT
        } else {
            super.addCopyrightFooter(x, width, parent)
        }
    }

    private fun addTitleCell(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val shadow = CellBorder(2.5, 2.5, rect.width - 6.0, rect.height - 6.0,
            0.0, FILL_LIGHT_GREY, 5.2,
            topTab = false,
            bottomTab = false,
            bevelTopLeft = true,
            bevelTopRight = true,
            bevelBottomRight = true,
            bevelBottomLeft = true
        )
        val border = CellBorder(0.0, 0.0, rect.width - 5.0, rect.height - 5.0,
            0.0, FILL_DARK_GREY, 1.932,
            false,
            bottomTab = false,
            bevelTopLeft = true,
            bevelTopRight = true,
            bevelBottomRight = true,
            bevelBottomLeft = true
        )
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        addRect(rect.width * 0.05, 2.0, rect.width * 0.2,
            rect.height - padding - 6.0, stroke = SVGConstants.SVG_NONE_VALUE,
            id = "fluffImage", parent = g)
        val image = document.createElementNS(svgNS, SVGConstants.SVG_IMAGE_TAG)
        image.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE,
            (rect.width * 0.05).truncate())
        image.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE,
            "2")
        image.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, (rect.width * 0.2).truncate())
        image.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, (rect.height - padding - 6.0).truncate())
        image.setAttributeNS(null, "xlink:href", encodedFluffImage)
        image.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "defaultFluffImage")
        g.appendChild(image)
        val lineHeight = calcFontHeight(FONT_SIZE_VLARGE)
        val text = createTextElement(0.0, 0.0, "",
            FONT_SIZE_VLARGE, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE)
        val startY = (rect.height - padding - lineHeight * title.size) * 0.5 + lineHeight * 0.8
        for (line in title.withIndex()) {
            val tspan = document.createElementNS(null, SVGConstants.SVG_TSPAN_TAG)
            tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE,
                (rect.width * 0.55).truncate())
            tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE,
                (startY + line.index * lineHeight).truncate())
            tspan.textContent = line.value
            text.appendChild(tspan)
        }
        g.appendChild(text)
        rootElement.appendChild(g)
    }

    private fun addUnitGroups(rect: Cell) {
        for (i in 0 until unitCount) {
            val g = createTranslatedGroup(rect.x, rect.y + i * rect.height / unitCapacity)
            g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unit_$i")
            rootElement.appendChild(g)
        }
    }
}

class InfantryMultiSheet(size: PaperSize): MultiUnitSheet(size) {
    override val fileName = "conventional_infantry_default.svg"
    override val unitCount = 4
    override val unitCapacity = 4
    override val encodedFluffImage: String = ResourceBundle.getBundle(InfantryRecordSheet::class.java.name)
        .getString("soldier_image")
    override val title = (1..3).map{bundle.getString("infantry.title.$it")}.toList()
    override fun logoInFooter() = true
}

class InfantryMultiSheetTables(size: PaperSize): MultiUnitSheet(size) {
    override val fileName = "conventional_infantry_tables.svg"
    override val unitCount = 3
    override val unitCapacity = 4
    override val encodedFluffImage: String = ResourceBundle.getBundle(InfantryRecordSheet::class.java.name)
        .getString("soldier_image")
    override val title = (1..3).map { bundle.getString("infantry.title.$it") }.toList()
    override fun logoInFooter() = false

    private val burstFireCell = Cell(0.0, height() * 0.75,
        width() * 0.5, height() * 0.25 - footerHeight)
    private val weaponDamageCell = Cell(burstFireCell.rightX() + padding, burstFireCell.y + tabBevelY,
        width() * 0.5 - padding, height() * 0.25 - footerHeight - tabBevelY)

    override fun build() {
        super.build()
        InfantryBurstFireTable(this).draw(burstFireCell)
        InfantryWeaponDamageTable(this).draw(weaponDamageCell)
    }
}

class BAMultiSheet(size: PaperSize) : MultiUnitSheet(size) {
    override val fileName = "battle_armor_default.svg"
    override val unitCount = 5
    override val unitCapacity = 5
    override val encodedFluffImage: String = ResourceBundle.getBundle(BattleArmorRecordSheet::class.java.name)
        .getString("ba_image")
    override val title = (1..2).map { bundle.getString("ba.title.$it") }.toList()
    override fun logoInFooter() = false

    private val legAttackTableCell = Cell(width() * 2.0 / 3.0 + padding,
        logoHeight + padding * 2,
        width() / 3.0 - padding, (height() - logoHeight - footerHeight - padding) * 0.13 - padding)
    private val swarmAttackTableCell = Cell(legAttackTableCell.x, legAttackTableCell.bottomY() + padding,
        legAttackTableCell.width, (height() - logoHeight - footerHeight - padding) * 0.11 - padding)
    private val swarmAttackModsCell = Cell(legAttackTableCell.x, swarmAttackTableCell.bottomY() + padding,
        legAttackTableCell.width, (height() - logoHeight - footerHeight - padding) * 0.3 - padding)
    private val swarmAttackLocCell = Cell(legAttackTableCell.x, swarmAttackModsCell.bottomY() + padding,
        legAttackTableCell.width, (height() - logoHeight - footerHeight - padding) * 0.22 - padding)
    private val transportLocCell = Cell(legAttackTableCell.x, swarmAttackLocCell.bottomY() + padding,
        legAttackTableCell.width, (height() - logoHeight - footerHeight - padding) * 0.24 - padding)

    override fun build() {
        super.build()
        LegAttacksTable(this).draw(legAttackTableCell)
        SwarmAttacksTable(this).draw(swarmAttackTableCell)
        SwarmAttackModsTable(this).draw(swarmAttackModsCell)
        SwarmAttackLocTable(this).draw(swarmAttackLocCell)
        TransportLocTable(this).draw(transportLocCell)
    }
}

internal class ProtoMechMultiSheet(size: PaperSize): MultiUnitSheet(size) {
    override val fileName = "protomech_default.svg"
    override val unitCount = 5
    override val unitCapacity = 5
    override val encodedFluffImage: String = bundle.getString("protomech.fluff")
    override val title = (1..2).map{bundle.getString("protomech.title.$it")}.toList()
    override fun logoInFooter() = true
}
