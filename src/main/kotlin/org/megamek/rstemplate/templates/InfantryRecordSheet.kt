package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class InfantryRecordSheet (size: PaperSize, color: Boolean) : RecordSheet(size, color) {
    override val fileName = "conventional_infantry_platoon.svg"

    protected val bundle = ResourceBundle.getBundle(InfantryRecordSheet::class.java.name)
    override fun fullPage() = false
    override fun showLogo() = false
    override fun showFooter() = false
    override fun height(): Double = super.height() * 0.25 + tabBevelY - padding

    override fun build() {
        val inner = addTabbedBorder()
        addDamagePanel(inner.x + inner.width * 0.2, inner.y + inner.height * 0.05,
            inner.width * 0.8 - padding, inner.height * 0.8)
    }

    fun addTabbedBorder(): Cell {
        val g = createTranslatedGroup(LEFT_MARGIN.toDouble(), 0.0)
        val label = RSLabel(this,2.5, 3.0, bundle.getString("panel.title"),
            FONT_SIZE_TAB_LABEL, width = width() * 0.5, textId = "type")
        val shadow = CellBorder(2.5, 2.5, width() - 2.5, height() - 6.0 - tabBevelY,
            label.rectWidth + 4, FILL_LIGHT_GREY, 5.2,
            true, true, true, true, true, true, width() * 0.5)
        val border = CellBorder(0.0, 0.0, width() - 2.5, height() - 5.0 - tabBevelY,
            label.rectWidth + 4, FILL_DARK_GREY, 1.932,
            true, true, true, true,true, true, width() * 0.5)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
        return Cell(LEFT_MARGIN.toDouble(), 0.0, width(), height() - tabBevelY)
            .inset(3.0, 5.0,3.0 + label.textHeight * 2, 5.0)
    }

    fun addDamagePanel(x: Double, y: Double, width: Double, height: Double) {
        val g = createTranslatedGroup(x, y)
        g.appendChild(CellBorder(0.0, 0.0, width, height, 0.0, strokeWidth = 1.0,
            topTab = false, bevelTopLeft = false, bevelTopRight = false).draw(document))
        var path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1.0")
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M 0,${height * 0.35} h $width")
        g.appendChild(path)
        path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1.0")
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M 0,${height * 0.5} h $width")
        g.appendChild(path)
        val yIndex = calcFontHeight(FONT_SIZE_SMALL) * 1.2
        val yDamage = height * 0.425 + calcFontHeight(FONT_SIZE_LARGE) * 0.4
        for (i in 0..29) {
            val xpos = i * width / 30.0
            path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
            path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
            path.setAttributeNS(null, SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1.0")
            path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                "M $xpos,0 v ${height * 0.5}")
            g.appendChild(path)
            addTextElement(xpos + padding, yIndex, (30 - i).toString(), FONT_SIZE_SMALL, parent = g)
            addTextElement(xpos + width / 60.0, yDamage, "\u2014", FONT_SIZE_LARGE,
                id = "damage_${30 - i}", anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
            addSoldierImages(30 - i, xpos + 1, yIndex + 1,
                width / 30.0 - 2, height * 0.35 - yIndex - 2, g)
        }
        document.documentElement.appendChild(g)
    }

    private fun addSoldierImages(index: Int, x: Double, y: Double, width: Double, height: Double, parent: Element) {
        var image = document.createElementNS(svgNS, SVGConstants.SVG_IMAGE_TAG)
        image.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, width.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, height.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "no_soldier$index")
        image.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY,
            SVGConstants.CSS_HIDDEN_VALUE)
        image.setAttributeNS(null, "xlink:${SVGConstants.XLINK_HREF_ATTRIBUTE}",
            bundle.getString("no_soldier_image"))
        parent.appendChild(image)
        image = document.createElementNS(svgNS, SVGConstants.SVG_IMAGE_TAG)
        image.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, width.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, height.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "soldier$index")
        image.setAttributeNS(null, "xlink:${SVGConstants.XLINK_HREF_ATTRIBUTE}",
            bundle.getString("soldier_image"))
        parent.appendChild(image)
    }
}