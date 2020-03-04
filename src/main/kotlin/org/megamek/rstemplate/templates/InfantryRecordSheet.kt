package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
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
    final override fun height(): Double = super.height() * 0.25 + tabBevelY - padding

    override fun build() {
        val inner = addTabbedBorder()
    }

    fun addTabbedBorder(): Cell {
        val g = createTranslatedGroup(LEFT_MARGIN.toDouble(), 0.0)
        val label = RSLabel(this,2.5, 3.0, bundle.getString("panel.title"),
            FONT_SIZE_TAB_LABEL, width = width() * 0.5, textId = "type")
        val shadow = CellBorder(2.5, 2.5, width() - 2.5, height() - 6.0 - tabBevelY,
            label.rectWidth + 4, FILL_LIGHT_GREY, 5.2,
            true, true, true, true, true, width() * 0.5)
        val border = CellBorder(0.0, 0.0, width() - 2.5, height() - 5.0 - tabBevelY,
            label.rectWidth + 4, FILL_DARK_GREY, 1.932,
            true, true, true, true, true, width() * 0.5)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
        return Cell(LEFT_MARGIN.toDouble(), 0.0, width(), height() - tabBevelY)
            .inset(3.0, 5.0,3.0 + label.textHeight * 2, 5.0)
    }
}