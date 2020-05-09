package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
internal abstract class ProtoMechRecordSheet(size: PaperSize, color: Boolean): RecordSheet(size, color) {
    protected val bundle = ResourceBundle.getBundle(ProtoMechRecordSheet::class.java.name)
    override fun fullPage() = false
    override fun showLogo() = false
    override fun showFooter() = false
    override fun height(): Double = (super.height() - logoHeight - footerHeight) * 0.25 - padding * 5
    abstract val armorDiagramFileName: String

    override fun build() {
        val inner = addTabbedBorder()
        val textCell = Cell(inner.x, inner.y, inner.width / 6.0, inner.height * 0.6);
        val inventoryCell = Cell(textCell.rightX(), inner.y - tabBevelY + padding,
            inner.width / 3.0, textCell.height + tabBevelY * 2.0 - padding)
        val hitCell = Cell(inventoryCell.rightX(), inner.y - tabBevelY + padding,
            inventoryCell.width, inner.height + tabBevelY * 2.0 - padding)
        val armorCell = Cell(hitCell.rightX(), inner.y - tabBevelY + padding,
            inner.width / 6.0, inner.height + tabBevelY * 2.0 - padding)
        val pilotCell = Cell(inner.x, textCell.bottomY(), inner.width * 0.5, inner.height * 0.4)
        addTextFields(textCell)
        addInventoryPanel(inventoryCell)
        addHitPanel(hitCell)
        addArmorDiagram(armorCell)
        addPilotData(pilotCell, textCell.width - tabBevelX * 2.0 - 4.0)
    }

    fun addTabbedBorder(): Cell {
        val g = createTranslatedGroup(0.0, 0.0)
        val label = RSLabel(this,2.5, 3.0, bundle.getString("panel.title"),
            FONT_SIZE_TAB_LABEL, textId = "type", fixedWidth = false,
            centerText = false)
        val labelWidthBelow = RSLabel(this, 2.5, 3.0, bundle.getString("panel.title"), FONT_SIZE_TAB_LABEL,
            width = null).rectWidth + 4.0;
        val shadow = CellBorder(2.5, 2.5, width() - 2.5, height() - 6.0 - tabBevelY,
            label.rectWidth + 4, FILL_LIGHT_GREY, 5.2,
            true, true, true, true, true, true,
            labelWidthBelow = labelWidthBelow)
        val border = CellBorder(0.0, 0.0, width() - 2.5, height() - 5.0 - tabBevelY,
            label.rectWidth + 4, FILL_DARK_GREY, 1.932,
            true, true, true, true,true, true,
            labelWidthBelow = labelWidthBelow)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
        return Cell(0.0, 0.0, width(), height() - tabBevelY)
            .inset(3.0, 5.0,3.0 + label.textHeight * 2, 5.0)
    }

    fun addTextFields(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val lineHeight = rect.height / 7.0
        var ypos = lineHeight
        addField(bundle.getString("type"), "type", rect.x, ypos, FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = rect.width - rect.x - padding, parent = g)
        ypos += lineHeight
        addField(bundle.getString("tons"), "tonnage", rect.x, ypos, FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            defaultText = "0", maxWidth = rect.width - rect.x - padding, parent = g)
        ypos += lineHeight
        addField(bundle.getString("role"), "role", rect.x, ypos, FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = rect.width - rect.x - padding, parent = g)
        ypos += lineHeight
        addTextElement(rect.x, ypos, bundle.getString("movementPoints"), FONT_SIZE_LARGE,
            SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += lineHeight
        addMPFields(rect.x + padding, ypos, g)
        document.documentElement.appendChild(g)
    }

    open fun addMPFields(x: Double, y: Double, parent: Element) {
        addFieldSet(listOf(
            LabeledField(bundle.getString("walk"), "mpWalk", "0"),
            LabeledField(bundle.getString("run"), "mpRun", "0"),
            LabeledField(bundle.getString("jump"), "mpJump", "0",
                labelId="textJumpMP")
        ), x, y, FONT_SIZE_LARGE, FILL_DARK_GREY, 50.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
    }

    fun addInventoryPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("inventoryPanel.title"), topTab = false,
            fontSize = FONT_SIZE_FREE_LABEL, parent = g)
        addRect(internal.x, internal.y, internal.width - padding, internal.height,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g)
        document.documentElement.appendChild(g)
    }

    fun addHitPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("hitPanel.title"), topTab = false,
            fontSize = FONT_SIZE_FREE_LABEL, parent = g)
        document.documentElement.appendChild(g)
    }

    fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(this, rect.width * 0.5, 0.0, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true)
        g.appendChild(label.draw())
        embedImage(0.0, label.height(), rect.width, rect.height - label.height() - padding,
            armorDiagramFileName, ImageAnchor.CENTER, g)
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
    }

    fun addPilotData(rect: Cell, tabWidth: Double) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val internal = addBorder(0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("pilotPanel.title"),
            fontSize = FONT_SIZE_FREE_LABEL, tabWidth = tabWidth, equalBevels = true,
            parent = g)
        document.documentElement.appendChild(g)
    }
}

internal class BipedProtoMechRecordSheet(size: PaperSize, color: Boolean):
        ProtoMechRecordSheet(size, color) {
    override val fileName = "protomech_biped.svg"
    override val armorDiagramFileName = "armor_diagram_protomech_biped.svg"
}

internal class GliderProtoMechRecordSheet(size: PaperSize, color: Boolean):
        ProtoMechRecordSheet(size, color) {
    override val fileName = "protomech_glider.svg"
    override val armorDiagramFileName = "armor_diagram_protomech_biped.svg"

    override fun addMPFields(x: Double, y: Double, parent: Element) {
        addFieldSet(listOf(
            LabeledField(bundle.getString("ground"), "mpGround", "1"),
            LabeledField(bundle.getString("cruise"), "mpWalk", "0"),
            LabeledField(bundle.getString("flank"), "mpRun", "0",
                labelId="textJumpMP")
        ), x, y, FONT_SIZE_MEDIUM, FILL_DARK_GREY, 50.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
    }
}