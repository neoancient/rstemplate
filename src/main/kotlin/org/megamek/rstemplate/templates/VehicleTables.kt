package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 * Game tables that are used on the bottom half of a page that does not have a second
 * vehicle record sheet
 */

abstract class VehicleTables(size: PaperSize): RecordSheet(size) {
    private val hitLocation = Cell(0.0, padding,
        width() * 0.57, (height() - footerHeight) * 0.6 - padding)
    val motiveDamage = Cell(hitLocation.rightX() + padding, hitLocation.y,
        width() - hitLocation.width - padding, hitLocation.height)
    private val criticalHits = Cell(hitLocation.x, hitLocation.bottomY() + padding,
        width(), height() - footerHeight - hitLocation.bottomY() - padding)

    final override fun height() = super.height() * 0.5 - padding
    override fun fullPage() = false
    override fun showLogo() = false
    override fun colorElements() = ""
    protected val bundle = ResourceBundle.getBundle(VehicleTables::class.java.name)

    abstract val unitType: String
    abstract val hitLocationTable: VeeHitLocationTable
    abstract val criticalHitTable: VeeCriticalHitTable

    override fun build() {
        hitLocationTable.draw(hitLocation)
        addMotiveDamageTable(motiveDamage)
        criticalHitTable.draw(criticalHits)
    }

    open fun addMotiveDamageTable(rect: Cell) {
        TankMotiveDamageTable(this).draw(rect)
    }
}

class TankTables(size: PaperSize): VehicleTables(size) {
    override val fileName = "tables_tank.svg"
    override val unitType = "tank"
    override val hitLocationTable = TankHitLocationTable(this)
    override val criticalHitTable = TankCriticalHitTable(this)
}

class VTOLTables(size: PaperSize): VehicleTables(size) {
    val elevation = Cell(motiveDamage.x, motiveDamage.y,
        motiveDamage.width, motiveDamage.height * 0.6 - padding)
    val physicals = Cell(motiveDamage.x, elevation.height + padding,
        motiveDamage.width, motiveDamage.height - elevation.height - padding)

    override fun addMotiveDamageTable(rect: Cell) {
        addElevationTable(elevation)
        addPhysicalsTable(physicals)
    }

    override val fileName = "tables_vtol.svg"
    override val unitType = "vtol"
    override val hitLocationTable = VTOLHitLocationTable(this)
    override val criticalHitTable = VTOLCriticalHitTable(this)

    private fun addElevationTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("elevationTable.title"), false, false,
            parent = g)
        addElevationTrack(padding * 1.5, inner.y + inner.height * 0.1,
            inner.width - padding * 2, inner.height * 0.3, (1..15).toList(), g)
        addElevationTrack(padding * 1.5, inner.y + inner.height * 0.55,
            inner.width - padding * 2, inner.height * 0.3, (16..30).toList(), g)
        rootElement.appendChild(g)
    }

    private fun addElevationTrack(x: Double, y: Double,
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
            FILL_DARK_GREY
        )
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "0.58")
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        parent.appendChild(grid)
        val fontSize = FONT_SIZE_MEDIUM
        val startX = colOffset + colWidth * 0.5
        val startY = y + (height - calcFontHeight(fontSize)) * 0.5 - 1
        addTextElement(x + padding, startY, bundle.getString("turn"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        addTextElement(x + padding, height / 2.0 + startY, bundle.getString("elevation"),
            fontSize, SVGConstants.SVG_BOLD_VALUE, parent = parent)
        for (i in 0 until range.size) {
            addTextElement(startX + i * colWidth, startY, range[i].toString(), fontSize,
                SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
                parent = parent)
        }
    }

    private fun addPhysicalsTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("physicalsTable.title"), false, false,
            parent = g)
        val lineHeight = calcFontHeight(FONT_SIZE_VSMALL)
        createTable(inner.x, inner.y + (inner.height - lineHeight * 5) * 0.5,
            inner.width, FONT_SIZE_VSMALL, listOf(
            listOf("-1 " + bundle.getString("orLower"), bundle.getString("none")),
            listOf("0", bundle.getString("noPunch")),
            listOf("1-2", bundle.getString("noKick")),
            listOf("3", bundle.getString("clubOnly")),
            listOf("4+", bundle.getString("none"))),
            listOf(0.17, 0.7),
            listOf(bundle.getString("differenceInLevels"), bundle.getString("attackTypes")),
            SVGConstants.SVG_MIDDLE_VALUE, true, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        rootElement.appendChild(g)
    }
}
