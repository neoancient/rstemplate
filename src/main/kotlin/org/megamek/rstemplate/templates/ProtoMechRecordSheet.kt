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
        val textCell = Cell(inner.x, inner.y, inner.width / 6.0, inner.height * 0.65);
        val inventoryCell = Cell(
            textCell.rightX(), inner.y - tabBevelY + padding,
            inner.width / 3.0, textCell.height + tabBevelY * 2.0 - padding * 2.0
        )
        val hitCell = Cell(
            inventoryCell.rightX(), inner.y - tabBevelY + padding,
            inventoryCell.width, inner.height + tabBevelY * 2.0 - padding * 2.0
        )
        val armorCell = Cell(
            hitCell.rightX(), inner.y - tabBevelY + padding,
            inner.width / 6.0, inner.height + tabBevelY * 2.0 - padding * 2.0
        )
        val pilotCell = Cell(
            inner.x, textCell.bottomY(),
            inner.width * 0.5, inner.height * 0.35 - padding
        )
        addTextFields(textCell)
        addInventoryPanel(inventoryCell)
        addHitPanel(hitCell)
        addArmorDiagram(armorCell)
        addPilotData(pilotCell, textCell.width - tabBevelX * 2.0 - 4.0)
    }

    fun addTabbedBorder(): Cell {
        val g = createTranslatedGroup(0.0, 0.0)
        val label = RSLabel(
            this, 2.5, 3.0, bundle.getString("panel.title"),
            FONT_SIZE_TAB_LABEL, textId = "type", fixedWidth = false,
            centerText = false
        )
        val labelWidthBelow = RSLabel(
            this, 2.5, 3.0, bundle.getString("panel.title"), FONT_SIZE_TAB_LABEL,
            width = null
        ).rectWidth + 4.0;
        val shadow = CellBorder(
            2.5, 2.5, width() - 2.5, height() - 6.0 - tabBevelY,
            label.rectWidth + 4, FILL_LIGHT_GREY, 5.2,
            true, true, true, true, true, true,
            labelWidthBelow = labelWidthBelow
        )
        val border = CellBorder(
            0.0, 0.0, width() - 2.5, height() - 5.0 - tabBevelY,
            label.rectWidth + 4, FILL_DARK_GREY, 1.932,
            true, true, true, true, true, true,
            labelWidthBelow = labelWidthBelow
        )
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
        return Cell(0.0, 0.0, width(), height() - tabBevelY)
            .inset(3.0, 5.0, 3.0 + label.textHeight * 2, 5.0)
    }

    fun addTextFields(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val lineHeight = rect.height / 7.0
        var ypos = lineHeight
        addField(
            bundle.getString("type"), "type", rect.x, ypos, FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = rect.width - rect.x - padding, parent = g
        )
        ypos += lineHeight
        addField(
            bundle.getString("tons"), "tonnage", rect.x, ypos, FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            defaultText = "0", maxWidth = rect.width - rect.x - padding, parent = g
        )
        ypos += lineHeight
        addField(
            bundle.getString("role"), "role", rect.x, ypos, FONT_SIZE_LARGE, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = rect.width - rect.x - padding, parent = g
        )
        ypos += lineHeight
        addTextElement(
            rect.x, ypos, bundle.getString("movementPoints"), FONT_SIZE_LARGE,
            SVGConstants.SVG_BOLD_VALUE, parent = g
        )
        ypos += lineHeight
        addMPFields(rect.x + padding, ypos, g)
        document.documentElement.appendChild(g)
    }

    open fun addMPFields(x: Double, y: Double, parent: Element) {
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("walk"), "mpWalk", "0"),
                LabeledField(bundle.getString("run"), "mpRun", "0"),
                LabeledField(
                    bundle.getString("jump"), "mpJump", "0",
                    labelId = "textJumpMP"
                )
            ), x, y, FONT_SIZE_LARGE, FILL_DARK_GREY, 50.0,
            SVGConstants.SVG_MIDDLE_VALUE, parent = parent
        )
    }

    fun addInventoryPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(
            0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("inventoryPanel.title"), topTab = false,
            fontSize = FONT_SIZE_FREE_LABEL, dropShadow = false, parent = g
        )
        addRect(
            inner.x, inner.y, inner.width - padding, inner.height,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g
        )
        document.documentElement.appendChild(g)
    }

    fun addHitPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(
            0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("hitPanel.title"), topTab = false,
            fontSize = FONT_SIZE_FREE_LABEL, dropShadow = false, parent = g
        )
        document.documentElement.appendChild(g)
    }

    fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(
            this, rect.width * 0.5, 0.0, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true
        )
        g.appendChild(label.draw())
        embedImage(
            0.0, label.height(), rect.width, rect.height - label.height() - padding,
            armorDiagramFileName, ImageAnchor.CENTER, g
        )
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
    }

    fun addPilotData(rect: Cell, tabWidth: Double) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(
            0.0, 0.0, rect.width - padding, rect.height,
            bundle.getString("pilotPanel.title"),
            fontSize = FONT_SIZE_FREE_LABEL, tabWidth = tabWidth, dropShadow = false,
            equalBevels = true, parent = g
        )
        val fontSize = FONT_SIZE_LARGE
        val lineHeight = inner.height / 3.0
        addField(
            bundle.getString("name"), "pilotName0",
            padding, inner.y + lineHeight, fontSize,
            blankId = "blankCrewName0",
            blankWidth = inner.width * 0.45 - padding * 2
                    - calcTextLength(
                "${bundle.getString("name")}_",
                fontSize, SVGConstants.SVG_BOLD_VALUE
            ),
            labelFixedWidth = false, parent = g
        )
        addField(
            bundle.getString("gunnerySkill"), "gunnerySkill0",
            padding,
            inner.y + lineHeight * 2, fontSize, defaultText = "0",
            blankId = "blankGunnerySkill0", labelId = "gunnerySkillText0",
            blankWidth = inner.width * 0.13, parent = g
        )
        addPilotDamageTrack(
            inner.x + inner.width * 0.45, inner.y + (inner.height - bevelY - 20) * 0.5,
            inner.width * 0.5, parent = g
        )
        document.documentElement.appendChild(g)
    }

    private fun addPilotDamageTrack(x: Double, y: Double, width: Double, parent: Element) {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        val chartBounds = Cell(
            x + width * 0.35, y,
            width * 0.65 - padding, 20.0
        )
        val outline = RoundedBorder(
            chartBounds.x, chartBounds.y, chartBounds.width, chartBounds.height,
            1.015, 0.56, 1.0
        ).draw(document)
        g.appendChild(outline)
        val grid = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        grid.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${chartBounds.x.truncate()},${(chartBounds.y + chartBounds.height / 2.0).truncate()}"
                    + " l ${chartBounds.width.truncate()},0"
                    + (1..5).map {
                " M ${(chartBounds.x + it * chartBounds.width / 6.0).truncate()},${chartBounds.y.truncate()} l 0,${chartBounds.height.truncate()}"
            }.joinToString(" ")
        )
        grid.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        grid.setAttributeNS(
            null, SVGConstants.SVG_STROKE_ATTRIBUTE,
            FILL_DARK_GREY
        )
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "0.58")
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_MITER_VALUE)
        grid.setAttributeNS(null, SVGConstants.CSS_STROKE_LINECAP_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        g.appendChild(grid)
        val startx = chartBounds.x - chartBounds.width / 12.0
        val starty = chartBounds.y + calcFontHeight(5.8f)
        val cons = listOf("3", "5", "7", "10", "11", bundle.getString("dead"))
        for (i in 1..6) {
            addTextElement(
                startx + i * chartBounds.width / 6.0, starty,
                i.toString(), 5.8f, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g
            )
            addTextElement(
                startx + i * chartBounds.width / 6.0, starty + chartBounds.height / 2.0,
                cons[i - 1], 5.8f, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, width = chartBounds.width / 6.0 - 4.0, parent = g
            )
        }
        addTextElement(
            chartBounds.x - padding, starty, bundle.getString("hitsTaken"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g
        )
        addTextElement(
            chartBounds.x - padding, starty + chartBounds.height / 2.0, bundle.getString("consciousnessNum"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - padding, parent = g
        )
        parent.appendChild(g)
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