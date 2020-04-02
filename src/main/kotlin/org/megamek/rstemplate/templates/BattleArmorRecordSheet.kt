package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.Cell
import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.layout.RoundedBorder
import org.megamek.rstemplate.layout.tabBevelY
import java.util.*

/**
 *
 */
class BattleArmorRecordSheet(size: PaperSize, color: Boolean): RecordSheet(size, color) {
    override val fileName = "battle_armor.svg"

    protected val bundle = ResourceBundle.getBundle(BattleArmorRecordSheet::class.java.name)
    override fun fullPage() = false
    override fun showLogo() = false
    override fun showFooter() = false
    override fun height(): Double = (super.height() - logoHeight - footerHeight) * 0.2 - padding * 5

    override fun build() {
        val internal = addBorder(0.0, 0.0, width() * 0.65, height() - tabBevelY,
            bundle.getString("title"), true,true,
            textBelow = bundle.getString("title"), textId = "squad")
        addTextFields(Cell(internal.x, internal.y, internal.width * 0.5, internal.height))
        addDamagePanel(Cell(internal.x + internal.width * 0.5, internal.y,
            internal.width * 0.5, internal.height))
    }

    fun addTextFields(rect: Cell) {
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = calcFontHeight(fontSize)
        var ypos = rect.y + lineHeight + padding
        addField(bundle.getString("type"), "type", rect.x, ypos, fontSize, maxWidth = rect.width)
        ypos += lineHeight
        addField(bundle.getString("gunnerySkill"), "gunnerySkill0", rect.x + padding, ypos, fontSize,
            blankId = "blankGunnerySkill0", maxWidth = rect.width * 0.5 - padding)
        addField(bundle.getString("antiMechSkill"), "pilotingSkill0", rect.x + rect.width * 0.5 + padding,
            ypos, fontSize, blankId = "blankPilotingSkill0", maxWidth = rect.width * 0.5 - padding)
        ypos += lineHeight
        addField(bundle.getString("groundMP"), "walkMP", rect.x + padding, ypos, fontSize,
            maxWidth = rect.width * 0.5 - padding)
        addField(bundle.getString("jumpMP"), "jumpMP", rect.x + rect.width * 0.5 + padding,
            ypos, fontSize, maxWidth = rect.width * 0.5 - padding)
        addRect(padding, ypos, rect.width - padding, rect.height - ypos, id = "inventory")
        ypos = rect.bottomY() - lineHeight
        addTextElement(padding, ypos, bundle.getString("mechanized"),
            fontSize, fontStyle = SVGConstants.SVG_BOLD_VALUE)
        addTextElement(padding + rect.width * 0.35, ypos, bundle.getString("swarm"),
            fontSize, fontStyle = SVGConstants.SVG_BOLD_VALUE)
        addTextElement(padding + rect.width * 0.63, ypos, bundle.getString("leg"),
            fontSize, fontStyle = SVGConstants.SVG_BOLD_VALUE)
        addTextElement(padding + rect.width * 0.84, ypos, bundle.getString("ap"),
            fontSize, fontStyle = SVGConstants.SVG_BOLD_VALUE)
        ypos -= lineHeight * 0.8
        addCheckBox(padding + calcTextLength(bundle.getString("mechanized") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkMechanized")
        addCheckBox(padding + rect.width * 0.35 +
                calcTextLength(bundle.getString("swarm") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkSwarm")
        addCheckBox(padding + rect.width * 0.63 +
                calcTextLength(bundle.getString("leg") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkLeg")
        addCheckBox(padding + rect.width * 0.84 +
                calcTextLength(bundle.getString("ap") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkAP")
    }

    private fun addCheckBox(x: Double, y: Double, size: Double, id: String) {
        val box = RoundedBorder(x, y, size, size, 1.315, 0.726,
            0.96, FILL_DARK_GREY)
        document.documentElement.appendChild(box.draw(document))
        addTextElement(x + size * 0.5,y + size, "\u2713", FONT_SIZE_VLARGE, SVGConstants.SVG_BOLD_VALUE,
            anchor = SVGConstants.SVG_MIDDLE_VALUE, id = id)
    }

    fun addDamagePanel(rect: Cell) {

    }

}