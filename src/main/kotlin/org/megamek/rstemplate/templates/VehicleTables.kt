package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.PaperSize
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
    private val motiveDamage = Cell(hitLocation.rightX() + padding, hitLocation.y,
        width() - hitLocation.width - padding, hitLocation.height)
    private val criticalHits = Cell(hitLocation.x, hitLocation.bottomY() + padding,
        width().toDouble(), height() - footerHeight - hitLocation.bottomY() - padding)

    final override fun height() = size.height * 0.5 - TOP_MARGIN - padding
    override fun fullPage() = false
    override fun showLogo() = false
    protected val bundle = ResourceBundle.getBundle(VehicleTables::class.java.name)

    override fun build() {
        addHitLocationTable(hitLocation)
        addMotiveDamageTable(motiveDamage)
        addCriticalHitTable(criticalHits)
    }

    abstract val hitLocations: List<List<String>>

    fun addHitLocationTable(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("groundHitTable.title"), false, false,
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

        val styles = listOf(SVGConstants.SVG_NORMAL_VALUE, SVGConstants.SVG_ITALIC_VALUE)
        for (i in 1..4) {
            ypos += addParagraph(padding, ypos, inner.width - padding * 2, bundle.getString("hitLocNotes.$i"),
                FONT_SIZE_VSMALL, g)
        }
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
            bundle.getString("groundCriticalsTable.title"), false, false,
            parent = g)
        val lineHeight = calcFontHeight(FONT_SIZE_LARGE)
        var ypos = inner.y + lineHeight
        addTextElement(inner.width * 0.54, ypos, bundle.getString("locationHit"),
            FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += createTable(0.0, ypos, inner.width, FONT_SIZE_LARGE,
            listOf(
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
                    bundle.getString("fuelTank"), bundle.getString("turretBlownOff"))),
                listOf(0.15, 0.27, 0.45, 0.63, 0.81), listOf(bundle.getString("2d6Roll"), bundle.getString("front"),
                bundle.getString("side"), bundle.getString("rear"), bundle.getString("turret"))
                    .map{it.toUpperCase()},
            parent = g)
        ypos += addParagraph(inner.width * 0.1, ypos, inner.width, bundle.getString("criticalHitNote.1"), FONT_SIZE_VSMALL, g)
        addParagraph(inner.width * 0.1, ypos, inner.width, bundle.getString("criticalHitNote.2"), FONT_SIZE_VSMALL, g)
        document.documentElement.appendChild(g)
    }

    /**
     * Adds a text element with a series of values arrayed in a grid
     *
     * @param x The x coordinate of the top left corner of the table
     * @param y The y coordinate of the top left corner of the table
     * @param width The width of the table
     * @param fontSize The size of font to use for the table
     * @param values A list of rows, each row being a list of cell values
     * @param colOffsets The x offset of the beginning of each column, expressed as a fraction
     *                   of the table width. This list needs to be at least as long as the longest row.
     * @param headers If non-null, the list of values to use for the column headers, which will be bold
     * @param anchor The text anchor to use for each cell.
     * @param firstColBold If true, the first column will be bold
     * @param firstColAnchor Only used if {@code firstColBold} is true. Allows the first column
     *                       to have a different anchor than the remainder of the table. If null,
     *                       the value of {@code anchor} will be used.
     * @return The height of the table
     */
    fun createTable(x: Double, y: Double, width: Double, fontSize: Float,
                    values: List<List<String>>, colOffsets: List<Double>,
                    headers: List<String>? = null, anchor: String = SVGConstants.SVG_MIDDLE_VALUE,
                    firstColBold: Boolean = true, firstColAnchor: String? = null,
                    parent: Element = document.documentElement): Double {
        val lineHeight = calcFontHeight(fontSize)
        var ypos = 0.0
        val g = createTranslatedGroup(x, y)
        if (headers != null) {
            val text = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
            text.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE,
                formatStyle(fontSize, SVGConstants.SVG_BOLD_VALUE))
            text.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
            for (header in headers.withIndex()) {
                if (header.index > 0 || !firstColBold || firstColAnchor == null) {
                    text.appendChild(createTspan(width * colOffsets[header.index],ypos, header.value))
                }
            }
            g.appendChild(text)
            ypos += lineHeight
        }
        val rowYPos = ArrayList<Double>()
        val text = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        text.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE,
            formatStyle(fontSize))
        text.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
        for (row in values) {
            rowYPos.add(ypos)
            var lineCount = 0
            for (cell in row.withIndex()) {
                if (firstColBold && cell.index == 0) {
                    continue
                }
                val lines = cell.value.split("\n".toRegex())
                for (line in lines.withIndex()) {
                    text.appendChild(createTspan(width * colOffsets[cell.index],
                        ypos + lineHeight * line.index, line.value))
                }
                lineCount = max(lineCount, lines.size)
            }
            ypos += lineHeight * lineCount
        }
        if (firstColBold) {
            val colX = width * colOffsets[0]
            val colText = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
            colText.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE,
                formatStyle(fontSize, SVGConstants.SVG_BOLD_VALUE))
            colText.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, firstColAnchor ?: anchor)
            if (headers != null && firstColAnchor != null) {
                colText.appendChild(createTspan(colX, rowYPos[0] - lineHeight, headers[0]))
            }
            for (row in values.withIndex()) {
                colText.appendChild(createTspan(colX, rowYPos[row.index], row.value[0]))
            }
            g.appendChild(colText)
        }
        g.appendChild(text)
        parent.appendChild(g)
        return ypos
    }

    fun createTspan(x: Double, y: Double, text: String): Element {
        val tspan = document.createElementNS(null, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        tspan.textContent = text
        return tspan
    }

    /**
     * Adds a paragraph of multiline text. Anything between curly braces will be italicized.
     *
     * @param x The x coordinate of the top left corner of the text block
     * @param y The y coordinate of the top right corner of the text block
     * @param text The text of the paragraph. All but the last line will be justified
     * @param fontSize The size of the font to use for the text
     * @param parent The parent element to add this paragraph to
     * @return The height of the paragraph
     */
    fun addParagraph(x: Double, y: Double, width: Double, text: String, fontSize: Float,
                     parent: Element): Double {
        val styles = listOf(SVGConstants.SVG_NORMAL_VALUE, SVGConstants.SVG_ITALIC_VALUE)
        val lineHeight = calcFontHeight(fontSize)
        var ypos = y
        val lines = text.split("\n".toRegex())
        for (line in lines.withIndex()) {
            ypos += lineHeight
            val segments = line.value.split("[{}]".toRegex())
            val last = line.index == lines.size - 1
            if (segments.size > 1) {
                var styleIndex = if (line.value.startsWith("{")) 1 else 0
                val mult = if (last) 1.0 else (width - padding * 2) / getLineLength(segments, styleIndex == 1)
                var xpos = x
                for (seg in segments) {
                    if (seg.isEmpty()) continue
                    val textWidth = mult * calcTextLength(seg, fontSize, styles[styleIndex])
                    addTextElement(xpos, ypos, seg, fontSize,
                        rightJustified = !last,
                        width = if (last) null else textWidth, fontStyle = styles[styleIndex], parent = parent)
                    xpos += textWidth
                    styleIndex = 1 - styleIndex
                }
            } else {
                addTextElement(x, ypos, line.value, fontSize,
                    rightJustified = !last,
                    width = if (last) null else width - padding * 2, parent = parent)
            }
        }
        return ypos - y
    }

    fun getLineLength(segments: List<String>, startItalic: Boolean): Double {
        var italic = startItalic
        var length = 0.0
        for (seg in segments) {
            length += calcTextLength(seg, FONT_SIZE_VSMALL,
                if (italic) SVGConstants.SVG_ITALIC_VALUE else SVGConstants.SVG_NORMAL_VALUE)
            italic = !italic
        }
        return length
    }
}

class TankTables(size: PaperSize): VehicleTables(size) {
    override val fileName = "tables_tank.svg"
    override val hitLocations = listOf(
        listOf("2*",
            bundle.getString("front") + bundle.getString("critical"),
            bundle.getString("rear") + bundle.getString("critical"),
            bundle.getString("side") + bundle.getString("critical")),
        listOf("3",
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
}

abstract class VTOLTables(size: PaperSize): VehicleTables(size) {
    override val fileName = "tables_vtol.svg"

    override fun addMotiveDamageTable(rect: Cell) {

    }
}
