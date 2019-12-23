package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.layout.RoundedBorder
import org.w3c.dom.Element
import java.lang.Integer.max
import java.util.*

/**
 * Game tables that are used on the bottom half of a page that does not have a second
 * vehicle record sheet
 */

abstract class VehicleTables(size: PaperSize): RecordSheet(size) {
    private val hitLocation = Cell(LEFT_MARGIN.toDouble(), padding,
        width() * 0.57, (height() - footerHeight) * 0.6 - padding)
    val motiveDamage = Cell(hitLocation.rightX() + padding, hitLocation.y,
        width() - hitLocation.width - padding, hitLocation.height)
    private val criticalHits = Cell(hitLocation.x, hitLocation.bottomY() + padding,
        width().toDouble(), height() - footerHeight - hitLocation.bottomY() - padding)

    final override fun height() = size.height * 0.5 - TOP_MARGIN - padding
    override fun fullPage() = false
    override fun showLogo() = false
    protected val bundle = ResourceBundle.getBundle(VehicleTables::class.java.name)

    abstract val unitType: String
    abstract val hitLocNotesCount: Int
    abstract val critTable: List<List<String>>
    abstract val critTableOffsets: List<Double>
    abstract val critTableLocations: List<String>

    override fun build() {
        addHitLocationTable(hitLocation)
        addMotiveDamageTable(motiveDamage)
        addCriticalHitTable(criticalHits)
    }

    abstract val hitLocations: List<List<String>>

    fun addHitLocationTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("hitTable.$unitType.title"), false, false,
            parent = g)
        val lineHeight = calcFontHeight(FONT_SIZE_VSMALL)
        var ypos = inner.y + lineHeight
        addTextElement(inner.width * 0.55, ypos, bundle.getString("attackDirection"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            hitLocations, listOf(0.1, 0.3, 0.55, 0.8),
            listOf(bundle.getString("2d6Roll"),
                bundle.getString("dirFront"),
                bundle.getString("dirRear"),
                bundle.getString("dirSides")), parent = g)
        ypos += lineHeight

        val notesG = document.createElementNS(null, SVGConstants.SVG_G_TAG)
        var notesY = 0.0
        for (i in 1..hitLocNotesCount) {
            notesY += addParagraph(padding, notesY, inner.width - padding * 2, bundle.getString("hitLocNotes.$unitType.$i"),
                FONT_SIZE_VSMALL, notesG)
        }
        notesG.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE}(0,${(inner.bottomY() - notesY - padding -lineHeight).truncate()})")
        g.appendChild(notesG)
        document.documentElement.appendChild(g)
    }

    open fun addMotiveDamageTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("motiveSystemTable.title"), false, false,
            parent = g)
        val lineHeight = calcFontHeight(FONT_SIZE_VSMALL)
        var ypos = inner.y + lineHeight
        ypos += createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            listOf(
                listOf("2-5", bundle.getString("noEffect")),
                listOf("6-7", bundle.getString("motiveMinorDamage")),
                listOf("8-9", bundle.getString("motiveModerateDamage")),
                listOf("10-11", bundle.getString("motiveHeavyDamage")),
                listOf("12", bundle.getString("motiveMajorDamage"))
            ), listOf(0.15, 0.27), listOf(bundle.getString("2d6Roll"), bundle.getString("effect")),
            SVGConstants.SVG_START_VALUE, true, SVGConstants.SVG_MIDDLE_VALUE, g)
        createTable(0.0, ypos + lineHeight, inner.width * 0.5, FONT_SIZE_VSMALL,
            listOf(
                listOf(bundle.getString("hitFromRear"), "+1"),
                listOf(bundle.getString("hitFromSide"), "+2")
            ), listOf(0.1, 0.75), listOf(bundle.getString("attackDirectionModifier")),
            SVGConstants.SVG_START_VALUE, false, parent = g)
        ypos += createTable(inner.width * 0.5, ypos + lineHeight, inner.width * 0.5,
            FONT_SIZE_VSMALL,
            listOf(
                listOf(bundle.getString("trackedNaval"), "+0"),
                listOf(bundle.getString("wheeled"), "+2"),
                listOf(bundle.getString("hovercraftHydrofoil"), "+3"),
                listOf(bundle.getString("wige"), "+4")
            ), listOf(0.1, 0.75), listOf(bundle.getString("vehicleTypeModifier")),
            SVGConstants.SVG_START_VALUE, false, parent = g)
        ypos += lineHeight
        addParagraph(padding, ypos, inner.width - padding * 2, bundle.getString("motiveSystemNote"),
            4.8f, g)
        document.documentElement.appendChild(g)
    }

    fun addCriticalHitTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("criticalsTable.$unitType.title"), false, false,
            parent = g)
        val lineHeight = calcFontHeight(FONT_SIZE_LARGE)
        var ypos = inner.y + lineHeight
        addTextElement(inner.width * 0.54, ypos, bundle.getString("locationHit"),
            FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += createTable(0.0, ypos, inner.width, FONT_SIZE_LARGE,
            critTable, critTableOffsets, critTableLocations.map{it.toUpperCase()},
            parent = g)
        ypos += addParagraph(inner.width * 0.1, ypos, inner.width, bundle.getString("criticalHitNote.$unitType.1"), FONT_SIZE_VSMALL, g)
        addParagraph(inner.width * 0.1, ypos, inner.width, bundle.getString("criticalHitNote.$unitType.2"), FONT_SIZE_VSMALL, g)
        document.documentElement.appendChild(g)
    }

}

class TankTables(size: PaperSize): VehicleTables(size) {
    override val fileName = "tables_tank.svg"
    override val unitType = "tank"
    override val hitLocNotesCount = 4
    override val hitLocations = listOf(
        listOf("2*",
            bundle.getString("front") + bundle.getString("critical"),
            bundle.getString("rear") + bundle.getString("critical"),
            bundle.getString("side") + bundle.getString("critical")),
        listOf("3",
            bundle.getString("front") + "\u2020",
            bundle.getString("rear") + "\u2020",
            bundle.getString("side") + "\u2020"),
        listOf("4",
            bundle.getString("front") + "\u2020",
            bundle.getString("rear") + "\u2020",
            bundle.getString("side") + "\u2020"),
        listOf("5",
            bundle.getString("rightSide") + "\u2020",
            bundle.getString("leftSide") + "\u2020",
            bundle.getString("front") + "\u2020"),
        listOf("6",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("7",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("8",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side") + bundle.getString("critical") + "*"),
        listOf("9",
            bundle.getString("leftSide") + "\u2020",
            bundle.getString("rightSide") + "\u2020",
            bundle.getString("rear") + "\u2020"),
        listOf("10",
            bundle.getString("turret"),
            bundle.getString("turret"),
            bundle.getString("turret")),
        listOf("11",
            bundle.getString("turret"),
            bundle.getString("turret"),
            bundle.getString("turret")),
        listOf("12*",
            bundle.getString("turret") + bundle.getString("critical"),
            bundle.getString("turret") + bundle.getString("critical"),
            bundle.getString("turret") + bundle.getString("critical"))
    )

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
    override val hitLocNotesCount = 3
    override val hitLocations = listOf(
        listOf("2*",
            bundle.getString("front") + bundle.getString("critical"),
            bundle.getString("rear") + bundle.getString("critical"),
            bundle.getString("side") + bundle.getString("critical")),
        listOf("3",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020"),
        listOf("4",
            bundle.getString("turret") + "\u2021",
            bundle.getString("turret") + "\u2021",
            bundle.getString("turret") + "\u2021"),
        listOf("5",
            bundle.getString("rightSide") + "\u2020",
            bundle.getString("leftSide") + "\u2020",
            bundle.getString("front") + "\u2020"),
        listOf("6",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("7",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side")),
        listOf("8",
            bundle.getString("front"),
            bundle.getString("rear"),
            bundle.getString("side") + bundle.getString("critical") + "*"),
        listOf("9",
            bundle.getString("leftSide"),
            bundle.getString("rightSide"),
            bundle.getString("rear")),
        listOf("10",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020"),
        listOf("11",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020",
            bundle.getString("rotors") + "\u2020"),
        listOf("12*",
            bundle.getString("rotors") + bundle.getString("critical") + "*\u2020",
            bundle.getString("rotors") + bundle.getString("critical") + "*\u2020",
            bundle.getString("rotors") + bundle.getString("critical") + "*\u2020"))

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

    private fun addElevationTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("elevationTable.title"), false, false,
            parent = g)
        addElevationTrack(padding * 1.5, inner.y + inner.height * 0.1,
            inner.width - padding * 2, inner.height * 0.3, (1..15).toList(), g)
        addElevationTrack(padding * 1.5, inner.y + inner.height * 0.55,
            inner.width - padding * 2, inner.height * 0.3, (16..30).toList(), g)
        document.documentElement.appendChild(g)
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
            SVGConstants.SVG_MIDDLE_VALUE, true, SVGConstants.SVG_MIDDLE_VALUE, g)
        document.documentElement.appendChild(g)
    }
}
