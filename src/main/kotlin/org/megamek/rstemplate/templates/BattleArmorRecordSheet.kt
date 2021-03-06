package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class BattleArmorRecordSheet(size: PaperSize): RecordSheet(size) {
    override val fileName = "battle_armor_squad.svg"

    private val bundle = ResourceBundle.getBundle(BattleArmorRecordSheet::class.java.name)
    override fun fullPage() = false
    override fun showLogo() = false
    override fun showFooter() = false
    override fun height(): Double = (super.height() - logoHeight - footerHeight) * 0.2 - padding * 5
    override fun colorElements() = ""

    override fun build() {
        val internal = addBorder(0.966, 0.966, width() * 2.0 / 3.0, height() - tabBevelY,
            bundle.getString("title"), topTab = true, bottomTab = true,
            textBelow = bundle.getString("title"), textId = "squad")
        addTextFields(Cell(internal.x, internal.y, internal.width * 0.5 - padding, internal.height))
        addDamagePanel(Cell(internal.x + internal.width * 0.5 - padding, internal.y - tabBevelY + padding,
            internal.width * 0.5, internal.height + tabBevelY - padding))
    }

    private fun addTextFields(rect: Cell) {
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = calcFontHeight(fontSize)
        var ypos = rect.y + lineHeight + padding
        addField(bundle.getString("type"), "type", rect.x, ypos, fontSize, maxWidth = rect.width)
        ypos += lineHeight
        addField(bundle.getString("gunnerySkill"), "gunnerySkill0", rect.x + padding, ypos, fontSize,
            blankId = "blankGunnerySkill0", blankWidth = rect.width * 0.5 - padding -
                    calcTextLength(bundle.getString("gunnerySkill") + "_", fontSize))
        addField(bundle.getString("antiMechSkill"), "pilotingSkill0", rect.x + rect.width * 0.5 + padding,
            ypos, fontSize, blankId = "blankPilotingSkill0", blankWidth = rect.width * 0.5 - padding * 2 -
                    calcTextLength(bundle.getString("antiMechSkill") + "_", fontSize))
        ypos += lineHeight
        addField(bundle.getString("groundMP"), "mpWalk", rect.x + padding, ypos, fontSize,
            maxWidth = rect.width * 0.5 - padding)
        addField(bundle.getString("jumpMP"), "mp_2", rect.x + rect.width * 0.5 + padding,
            ypos, fontSize, maxWidth = rect.width * 0.5 - padding, labelId = "movement_mode_2")
        addRect(padding, ypos, rect.width - padding, rect.height - ypos + lineHeight * 0.5, id = "inventory")
        ypos = rect.bottomY() - lineHeight
        addTextElement(rect.x + padding, ypos, bundle.getString("mechanized"),
            fontSize, SVGConstants.SVG_BOLD_VALUE)
        addTextElement(rect.x + padding + rect.width * 0.35, ypos, bundle.getString("swarm"),
            fontSize, SVGConstants.SVG_BOLD_VALUE)
        addTextElement(rect.x + padding + rect.width * 0.63, ypos, bundle.getString("leg"),
            fontSize, SVGConstants.SVG_BOLD_VALUE)
        addTextElement(rect.x + padding + rect.width * 0.84, ypos, bundle.getString("ap"),
            fontSize, SVGConstants.SVG_BOLD_VALUE)
        ypos -= lineHeight * 0.8
        addCheckBox(rect.x +padding + calcTextLength(bundle.getString("mechanized") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkMechanized")
        addCheckBox(rect.x +padding + rect.width * 0.35 +
                calcTextLength(bundle.getString("swarm") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkSwarm")
        addCheckBox(rect.x +padding + rect.width * 0.63 +
                calcTextLength(bundle.getString("leg") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkLeg")
        addCheckBox(rect.x +padding + rect.width * 0.84 +
                calcTextLength(bundle.getString("ap") + "_", fontSize, SVGConstants.SVG_BOLD_VALUE),
            ypos, lineHeight.toDouble(), "checkAP")
        addRect(rect.rightX() - 20 - padding,rect.y - tabBevelY + 1.0,20.0, 20.0, id = "eraIcon")
    }

    private fun addCheckBox(x: Double, y: Double, size: Double, id: String) {
        val box = RoundedBorder(x, y, size, size, 1.315, 0.726,
            0.96, FILL_DARK_GREY)
        rootElement.appendChild(box.draw(document))
        addTextElement(x + size * 0.5,y + size, "\u2713", FONT_SIZE_VLARGE, SVGConstants.SVG_BOLD_VALUE,
            anchor = SVGConstants.SVG_MIDDLE_VALUE, id = id)
    }

    private fun addDamagePanel(rect: Cell) {
        val height = rect.height / 6.0 - 2.0
        var ypos = rect.y
        for (i in 0..5) {
            rootElement.appendChild(drawSuit(i, rect.x, ypos, rect.width, height))
            ypos += rect.height / 6.0
        }
        addField(bundle.getString("armor"), "armorType", rect.x, rect.bottomY() + tabBevelY - padding,
            FONT_SIZE_MEDIUM, defaultText = "Standard", maxWidth = rect.width * 0.5)
        addField(bundle.getString("bv"), "bv", rect.x + rect.width * 0.5, rect.bottomY() + tabBevelY - padding,
            FONT_SIZE_MEDIUM, defaultText = "0")
    }

    private fun drawSuit(index: Int, x: Double, y: Double, width: Double, height: Double): Element {
        val bevelX = 2.25
        val bevelY = 2.4
        val g = createTranslatedGroup(x, y)
        g.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "suit$index")
        val path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "0.966")
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
        path.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M 0,$bevelY l $bevelX,-$bevelY h 20 l $bevelX,$bevelY h ${width - 20 - bevelX * 3} l $bevelX,$bevelY" +
                    "v ${height - bevelY * 4} l -$bevelX,$bevelY h -${width - 20 - bevelX * 3} l -$bevelX,$bevelY" +
                    "h -20 l -$bevelX,-$bevelY Z")
        g.appendChild(path)
        addTextElement(9.0, height * 0.7, (index + 1).toString(),
            FONT_SIZE_TAB_LABEL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE, parent = g)
        addSuitImage(index,10.0, 1.0, 12.0, height -  2.0, g)
        addRect(20.0 + bevelX * 2, bevelY + 1.0, width - 20.0 - bevelX * 3, height - bevelY * 2 - 1,
            stroke = SVGConstants.SVG_NONE_VALUE, id = "pips_$index", parent = g)
        return g
    }

    private fun addSuitImage(index: Int, x: Double, y: Double, width: Double, height: Double, parent: Element) {
        val image = document.createElementNS(svgNS, SVGConstants.SVG_IMAGE_TAG)
        image.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, width.truncate())
        image.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, height.truncate())
        image.setAttributeNS(null, "xlink:${SVGConstants.XLINK_HREF_ATTRIBUTE}",
            bundle.getString("ba_image"))
        image.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "defaultFluffImage_$index")
        parent.appendChild(image)
        addRect(x, y, width, height, id = "fluffImage_$index", parent = parent)
    }
}