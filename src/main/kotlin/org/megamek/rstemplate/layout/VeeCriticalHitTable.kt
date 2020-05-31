package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_LARGE
import org.megamek.rstemplate.templates.FONT_SIZE_VSMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
abstract class VeeCriticalHitTable(private val sheet: RecordSheet) {
    abstract val unitType: String
    abstract val critTable: List<List<String>>
    abstract val critTableOffsets: List<Double>
    abstract val critTableLocations: List<String>
    protected val bundle: ResourceBundle = ResourceBundle.getBundle(VeeCriticalHitTable::class.java.name)

    abstract fun title(): String

    fun draw(rect: Cell, parent: Element = sheet.rootElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            title(), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = sheet.calcFontHeight(FONT_SIZE_LARGE)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.54, ypos, bundle.getString("locationHit"),
            FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_LARGE,
            critTable, critTableOffsets, critTableLocations.map{it.toUpperCase()},
            parent = g)
        ypos += sheet.addParagraph(inner.width * 0.1, ypos, inner.width, bundle.getString("criticalHitNote.$unitType.1"), FONT_SIZE_VSMALL, g)
        sheet.addParagraph(inner.width * 0.1, ypos, inner.width, bundle.getString("criticalHitNote.$unitType.2"), FONT_SIZE_VSMALL, g)
        parent.appendChild(g)
    }
}

open class TankCriticalHitTable(sheet: RecordSheet): VeeCriticalHitTable(sheet) {
    override val unitType = "tank"
    override val critTableOffsets = listOf(0.15, 0.27, 0.45, 0.63, 0.81)
    override val critTableLocations = listOf(bundle.getString("2d6Roll"), bundle.getString("front"),
        bundle.getString("side"), bundle.getString("rear"), bundle.getString("turret"))
    override val critTable = listOf(
        listOf("2-5", bundle.getString("noCritical"), bundle.getString("noCritical"),
            bundle.getString("noCritical"), bundle.getString("noCritical")),
        listOf("6", bundle.getString("driverHit"), bundle.getString("cargoHit"),
            bundle.getString("weaponMalfunction"), bundle.getString("stabilizer")),
        listOf("7", bundle.getString("weaponMalfunction"), bundle.getString("weaponMalfunction"),
            bundle.getString("cargoHit"), bundle.getString("turretJam")),
        listOf("8", bundle.getString("stabilizer"), bundle.getString("crewStunned"),
            bundle.getString("stabilizer"), bundle.getString("weaponMalfunction")),
        listOf("9", bundle.getString("sensors"), bundle.getString("stabilizer"),
            bundle.getString("weaponDestroyed"), bundle.getString("turretLocks")),
        listOf("10", bundle.getString("commanderHit"), bundle.getString("weaponDestroyed"),
            bundle.getString("engineHit"), bundle.getString("weaponDestroyed")),
        listOf("11", bundle.getString("weaponDestroyed"), bundle.getString("engineHit"),
            bundle.getString("ammunition"), bundle.getString("ammunition")),
        listOf("12", bundle.getString("crewKilled"), bundle.getString("fuelTank"),
            bundle.getString("fuelTank"), bundle.getString("turretBlownOff")))

    override fun title(): String = bundle.getString("criticalsTable.tank.title")
}

class NavalCriticalHitTable(sheet: RecordSheet): TankCriticalHitTable(sheet) {
    override fun title(): String = bundle.getString("criticalsTable.naval.title")
}

class VTOLCriticalHitTable(sheet: RecordSheet): VeeCriticalHitTable(sheet) {
    override val unitType = "vtol"
    override val critTableOffsets = listOf(0.1, 0.21, 0.35, 0.53, 0.71, 0.85)
    override val critTableLocations = listOf(bundle.getString("2d6Roll"), bundle.getString("front"),
        bundle.getString("side"), bundle.getString("rear"), bundle.getString("rotors"), bundle.getString("turret"))
    override val critTable = listOf(
        listOf("2-5", bundle.getString("noCritical"), bundle.getString("noCritical"),
            bundle.getString("noCritical"), bundle.getString("noCritical"), bundle.getString("noCritical")),
        listOf("6", bundle.getString("copilotHit"), bundle.getString("weaponMalfunction"),
            bundle.getString("cargoHit"), bundle.getString("rotorDamage"), bundle.getString("stabilizer")),
        listOf("7", bundle.getString("weaponMalfunction"), bundle.getString("cargoHit"),
            bundle.getString("weaponMalfunction"), bundle.getString("rotorDamage"), bundle.getString("turretJam")),
        listOf("8", bundle.getString("stabilizer"), bundle.getString("stabilizer"),
            bundle.getString("stabilizer"), bundle.getString("rotorDamage"), bundle.getString("weaponMalfunction")),
        listOf("9", bundle.getString("sensors"), bundle.getString("weaponDestroyed"),
            bundle.getString("weaponDestroyed"), bundle.getString("flightStabilizerHit"), bundle.getString("turretLocks")),
        listOf("10", bundle.getString("pilotHit"), bundle.getString("engineHit"),
            bundle.getString("sensors"), bundle.getString("flightStabilizerHit"), bundle.getString("weaponDestroyed")),
        listOf("11", bundle.getString("weaponDestroyed"), bundle.getString("rotorsDestroyed"), bundle.getString("ammunition"),
            bundle.getString("engineHit"), bundle.getString("ammunition")),
        listOf("12", bundle.getString("crewKilled"), bundle.getString("fuelTank"),
            bundle.getString("fuelTank"), bundle.getString("rotorsDestroyed"), bundle.getString("turretBlownOff")))

    override fun title(): String = bundle.getString("criticalsTable.vtol.title")
}