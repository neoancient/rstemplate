package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.CellBorder
import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.layout.tabBevelY
import java.util.*

/**
 *
 */
abstract class MultiUnitSheet(size: PaperSize, color: Boolean): RecordSheet(size, color) {

    private val titleCell = Cell(size.width - RIGHT_MARGIN - width() / 3.0, TOP_MARGIN.toDouble(),
        width() / 3.0, logoHeight)
    private val unitCell = Cell(LEFT_MARGIN.toDouble(), TOP_MARGIN + logoHeight + padding,
        size.width.toDouble(), height() - logoHeight - footerHeight - padding * 2)
    override fun showTitle() = false
    override final fun height() = super.height()

    protected val bundle = ResourceBundle.getBundle(MultiUnitSheet::class.java.name)
    abstract val unitCount: Int
    abstract val encodedFluffImage: String
    abstract val title: List<String>

    override fun build() {
        addTitleCell(titleCell)
        addUnitGroups(unitCell)
    }

    private fun addTitleCell(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val shadow = CellBorder(2.5, 2.5, rect.width - 2.5, rect.height - 2.5,
            0.0, FILL_LIGHT_GREY, 5.2,
            false, false, true, true, true, true)
        val border = CellBorder(0.0, 0.0, rect.width - 2.5, rect.height - 2.5,
            0.0, FILL_DARK_GREY, 1.932,
            false, false, true, true, true, true)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        addRect(rect.width * 0.6, 1.0, rect.width * 0.15,
            rect.height - padding - 2, stroke = SVGConstants.SVG_NONE_VALUE, id = "fluffImage")
        val image = document.createElementNS(svgNS, SVGConstants.SVG_IMAGE_TAG)
        image.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE,
            (rect.width * 0.06).truncate())
        image.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE,
            "1")
        image.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, (rect.width * 0.15).truncate())
        image.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, (rect.height - 2 - padding).truncate())
        image.setAttributeNS(null, "xlink:href", bundle.getString("infantry.fluff"))
        g.appendChild(image)
        val lineHeight = calcFontHeight(FONT_SIZE_VLARGE)
        val text = createTextElement(0.0, 0.0, "",
            FONT_SIZE_VLARGE, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE)
        val startY = (rect.height - padding - lineHeight * title.size) * 0.5 + lineHeight
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
        document.documentElement.appendChild(g)
    }

    private fun addUnitGroups(rect: Cell) {
        for (i in 0 until unitCount) {
            val g = createTranslatedGroup(rect.x, rect.y + i * rect.height / unitCount)
            g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "unit_$i")
            document.documentElement.appendChild(g)
        }
    }
}

class InfantryMultiSheet(size: PaperSize, color: Boolean): MultiUnitSheet(size, color) {
    override val fileName = "conventional_infantry_default.svg"
    override val unitCount = 4
    override val encodedFluffImage = ResourceBundle.getBundle(InfantryRecordSheet::class.java.name)
        .getString("soldier_image")
    override val title = (1..3).map{bundle.getString("infantry.title.$it")}.toList()
}