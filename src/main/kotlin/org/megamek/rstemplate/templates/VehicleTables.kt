package org.megamek.rstemplate.templates

import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.PaperSize
import java.util.*

/**
 * Game tables that are used on the bottom half of a page that does not have a second
 * vehicle record sheet
 */

abstract class VehicleTables(size: PaperSize): RecordSheet(size) {
    private val hitLocation = Cell(LEFT_MARGIN.toDouble(), padding,
        width() * 0.57, (height() - footerHeight) * 0.6 - padding)
    private val motiveDamage = Cell(hitLocation.rightX() + padding, hitLocation.y,
        width() - hitLocation.width - padding, hitLocation.height)
    private val criticalHits = Cell(hitLocation.x, hitLocation.bottomY() + padding,
        width().toDouble(), height() - footerHeight - hitLocation.bottomY())

    final override fun height() = size.height * 0.5 - TOP_MARGIN - padding
    override fun fullPage() = false
    protected val bundle = ResourceBundle.getBundle(VehicleTables::class.java.name)

    override fun build() {
        addHitLocationTable(hitLocation)
        addMotiveDamageTable(motiveDamage)
        addCriticalHitTable(criticalHits)
    }

    fun addHitLocationTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("groundHitTable.title"), false, false,
            parent = g)
        document.documentElement.appendChild(g)
    }

    open fun addMotiveDamageTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("motiveSystemTable.title"), false, false,
            parent = g)
        document.documentElement.appendChild(g)
    }

    fun addCriticalHitTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("groundCriticalsTable.title"), false, false,
            parent = g)
        document.documentElement.appendChild(g)
    }
}

class TankTables(size: PaperSize): VehicleTables(size) {
    override val fileName = "tables_tank.svg"
}

class VTOLTables(size: PaperSize): VehicleTables(size) {
    override val fileName = "tables_vtol.svg"

    override fun addMotiveDamageTable(rect: Cell) {

    }
}
