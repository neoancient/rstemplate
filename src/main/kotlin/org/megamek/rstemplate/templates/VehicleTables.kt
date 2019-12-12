package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.PaperSize
import org.w3c.dom.Element
import java.awt.Font
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
        val cols = listOf(0.1, 0.3, 0.55, 0.8).map{it * inner.width}
        val lineHeight = calcFontHeight(FONT_SIZE_VSMALL)
        var ypos = inner.y + lineHeight
        addTextElement(cols[2], ypos, bundle.getString("attackDirection"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        g.appendChild(createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            hitLocations, listOf(0.1, 0.3, 0.55, 0.8),
            listOf(bundle.getString("2d6Roll"),
                bundle.getString("dirFront"),
                bundle.getString("dirRear"),
                bundle.getString("dirSides"))))
        ypos += lineHeight * (hitLocations.size + 2)

        val styles = listOf(SVGConstants.SVG_NORMAL_VALUE, SVGConstants.SVG_ITALIC_VALUE)
        for (i in 1..4) {
            val note = bundle.getString("hitLocNotes.$i").split("\n".toRegex())
            for (line in note.withIndex()) {
                val segments = line.value.split("[{}]".toRegex())
                val last = line.index == note.size - 1
                if (segments.size > 1) {
                    var styleIndex = if (line.value.startsWith("{")) 1 else 0
                    val mult = if (last) 1.0 else (inner.width - padding * 2) / getLineLength(segments, styleIndex == 1)
                    var xpos = padding
                    for (seg in segments) {
                        if (seg.isEmpty()) continue
                        val width = mult * calcTextLength(seg, FONT_SIZE_VSMALL, styles[styleIndex])
                        addTextElement(xpos, ypos, seg, FONT_SIZE_VSMALL,
                            rightJustified = !last,
                            width = if (last) null else width, fontStyle = styles[styleIndex], parent = g)
                        xpos += width
                        styleIndex = 1 - styleIndex
                    }
                } else {
                    addTextElement(padding, ypos, line.value, FONT_SIZE_VSMALL,
                        rightJustified = !last,
                        width = if (last) null else inner.width - padding * 2, parent = g)
                }
                ypos += lineHeight
            }
        }
        document.documentElement.appendChild(g)
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

    fun createTable(x: Double, y: Double, width: Double, fontSize: Float,
                    values: List<List<String>>, colOffsets: List<Double>,
                    headers: List<String>? = null, anchor: String = SVGConstants.SVG_MIDDLE_VALUE,
                    firstColBold: Boolean = true): Element {
        val lineHeight = calcFontHeight(fontSize)
        var ypos = 0.0
        val g = createTranslatedGroup(x, y)
        if (headers != null) {
            val text = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
            text.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE,
                formatStyle(fontSize, SVGConstants.SVG_BOLD_VALUE))
            text.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
            for (header in headers.withIndex()) {
                text.appendChild(createTspan(width * colOffsets[header.index],
                    ypos, header.value))
            }
            g.appendChild(text)
            ypos += lineHeight
        }
        if (firstColBold) {
            val colX = width * colOffsets[0]
            var colY = ypos
            val text = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
            text.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE,
                formatStyle(fontSize, SVGConstants.SVG_BOLD_VALUE))
            text.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
            for (row in values) {
                text.appendChild(createTspan(colX, colY, row[0]))
                colY += lineHeight
            }
            g.appendChild(text)
        }
        val text = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        text.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE,
            formatStyle(fontSize))
        text.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
        for (row in values) {
            for (cell in row.withIndex()) {
                if (firstColBold && cell.index == 0) {
                    continue
                }
                text.appendChild(createTspan(width * colOffsets[cell.index],
                    ypos, cell.value))
            }
            ypos += lineHeight
        }
        g.appendChild(text)
        return g
    }

    fun createTspan(x: Double, y: Double, text: String): Element {
        val tspan = document.createElementNS(null, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        tspan.textContent = text
        return tspan
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
