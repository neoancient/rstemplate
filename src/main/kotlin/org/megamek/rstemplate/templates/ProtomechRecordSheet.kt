package org.megamek.rstemplate.templates

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Element
import java.util.*

const val INNER_STROKE_WIDTH = 0.95
const val PADDING = 2.0
/**
 * Creates template for ProtoMech record sheets
 */
internal abstract class ProtomechRecordSheet(size: PaperSize, color: Boolean): RecordSheet(size, color) {
    protected val bundle = ResourceBundle.getBundle(ProtomechRecordSheet::class.java.name)
    override fun fullPage() = false
    override fun showLogo() = false
    override fun showFooter() = false
    override fun height(): Double = (super.height() - logoHeight - footerHeight) * 0.2 - PADDING * 6
    abstract val armorDiagramFileName: String

    abstract fun isQuad(): Boolean
    abstract fun isGlider(): Boolean
    abstract fun locations(): Int

    override fun build() {
        val inner = addTabbedBorder()
        val textCell = Cell(inner.x, inner.y, inner.width / 6.0, inner.height * 0.65);
        val inventoryCell = Cell(
            textCell.rightX(), inner.y - tabBevelY + PADDING,
            inner.width / 3.0, textCell.height + tabBevelY * 2.0 - PADDING * 2.0
        )
        val hitCell = Cell(
            inventoryCell.rightX(), inner.y - tabBevelY + PADDING,
            inventoryCell.width, inner.height + tabBevelY * 2.0 - PADDING * 2.0
        )
        val armorCell = Cell(
            hitCell.rightX(), inner.y - tabBevelY + PADDING,
            inner.width / 6.0, inner.height + tabBevelY * 2.0 - PADDING * 2.0
        )
        val pilotCell = Cell(
            inner.x, textCell.bottomY(),
            inner.width * 0.5, inner.height * 0.35 - PADDING
        )
        addTextFields(textCell)
        addInventoryPanel(inventoryCell)
        addHitPanel(hitCell)
        addArmorDiagram(armorCell)
        addPilotData(pilotCell, textCell.width - tabBevelX * 2.0 - 4.0)
        addField(bundle.getString("bv"), "bv",inventoryCell.x + PADDING,
            inner.bottomY() + tabBevelY - PADDING, FONT_SIZE_SMALL, defaultText = "0")
        addField(bundle.getString("armor"), "armorType",pilotCell.x + pilotCell.width * 0.5,
            inner.bottomY() + tabBevelY - PADDING, FONT_SIZE_SMALL, defaultText = "Standard")
        addRect(inventoryCell.x - PADDING - 20.0, pilotCell.y - PADDING - 20.0,
            10.0,10.0, id = "eraIcon")
    }

    fun addTabbedBorder(): Cell {
        val g = createTranslatedGroup(0.0, 0.0)
        val label = RSLabel(
            this, 2.5, 3.0, bundle.getString("panel.title"),
            FONT_SIZE_TAB_LABEL, textId = "protomechIndex", fixedWidth = false,
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
        val lineHeight = rect.height / 8.0
        var ypos = lineHeight
        val fontSize = FONT_SIZE_LARGE
        addField(
            bundle.getString("type"), "type", rect.x, ypos, fontSize, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = rect.width - rect.x - PADDING, parent = g
        )
        ypos += lineHeight
        addTextElement(rect.x + rect.width * 0.5, ypos, "", fontSize,
            id = "type2", width = rect.width * 0.5 - PADDING, parent = g)
        addField(
            bundle.getString("tons"), "tonnage", rect.x, ypos, fontSize, SVGConstants.SVG_BOLD_VALUE,
            defaultText = "0", maxWidth = rect.width * 0.5 - rect.x - PADDING, parent = g
        )
        ypos += lineHeight
        addField(
            bundle.getString("role"), "role", rect.x, ypos, fontSize, SVGConstants.SVG_BOLD_VALUE,
            maxWidth = rect.width - rect.x - PADDING, labelId = "lblRole", parent = g
        )
        ypos += lineHeight
        addTextElement(
            rect.x, ypos, bundle.getString("movementPoints"), fontSize,
            SVGConstants.SVG_BOLD_VALUE, parent = g
        )
        ypos += lineHeight
        addMPFields(rect.x + PADDING, ypos, fontSize, g)
        document.documentElement.appendChild(g)
    }

    open fun addMPFields(x: Double, y: Double, fontSize: Float, parent: Element) {
        addFieldSet(
            listOf(
                LabeledField(bundle.getString("walk"), "mpWalk", "0"),
                LabeledField(bundle.getString("run"), "mpRun", "0"),
                LabeledField(
                    bundle.getString("jump"), "mpJump", "0",
                    labelId = "lblJump"
                )
            ), x, y, fontSize, FILL_DARK_GREY, 50.0,
            SVGConstants.SVG_MIDDLE_VALUE, labelFixedWidth = false, parent = parent
        )
    }

    fun addInventoryPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(
            0.0, 0.0, rect.width - PADDING, rect.height,
            bundle.getString("inventoryPanel.title"), topTab = false,
            fontSize = FONT_SIZE_FREE_LABEL, strokeWidth = INNER_STROKE_WIDTH,
            dropShadow = false, parent = g
        )
        addRect(
            inner.x, inner.y, inner.width - PADDING, inner.height,
            SVGConstants.SVG_NONE_VALUE, id = "inventory", parent = g
        )
        document.documentElement.appendChild(g)
    }

    fun addHitPanel(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(
            0.0, 0.0, rect.width - PADDING, rect.height,
            bundle.getString("hitPanel.title"), topTab = false,
            fontSize = FONT_SIZE_FREE_LABEL, strokeWidth = INNER_STROKE_WIDTH,dropShadow = false, parent = g)
        val fontHeight = calcFontHeight(FONT_SIZE_SMALL)
        val lineHeight = inner.height / (locations() + 4)
        val colXOffsets = listOf(0.06, 0.12, 0.3, 0.55, 0.8).map{
            inner.x + it * inner.width
        }.toList()
        var ypos = inner.y + fontHeight * 1.2
        // Ratio of the space available in a column to the longest string
        val kern = (0.25 * inner.width - fontHeight * 1.2 - PADDING) /
                calcTextLength(bundle.getString("halfJump"), FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE)
        addTextElement(colXOffsets[0], ypos, bundle.getString("2d6"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        addTextElement(colXOffsets[1], ypos, bundle.getString("location"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        addTextElement(colXOffsets[2], ypos, bundle.getString("1stHit"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        addTextElement(colXOffsets[3], ypos, bundle.getString("2ndHit"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        addTextElement(colXOffsets[4], ypos, bundle.getString("3rdHit"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += lineHeight
        addHitTableRow(colXOffsets, ypos, kern, "2", bundle.getString("mainGun"),
            listOf(FILL_WHITE), listOf(bundle.getString("mainGunDestroyed")), g)
        ypos += lineHeight
        if (isGlider()) {
            addHitTableRow(
                colXOffsets, ypos, kern, "3,11", bundle.getString("wings"),
                listOf(null),
                listOf(bundle.getString("wingHit")), g)
            ypos += lineHeight
        }
        if (!isQuad()) {
            addHitTableRow(
                colXOffsets, ypos, kern, "4", bundle.getString("rightArm"),
                listOf(FILL_WHITE, FILL_LIGHT_GREY),
                listOf(bundle.getString("toHitMod"), bundle.getString("rightArmDestroyed")), g
            )
            ypos += lineHeight
        }
        addHitTableRow(
            colXOffsets, ypos, kern, if (isQuad()) "4,5|9,10" else "5,9", bundle.getString("legs"),
            listOf(FILL_WHITE, FILL_WHITE, FILL_LIGHT_GREY),
            listOf(bundle.getString("walkMod"), bundle.getString("halfWalk"), bundle.getString("noMove")), g)
        ypos += lineHeight
        addHitTableRow(
            colXOffsets, ypos, kern, "6,7,8", bundle.getString("torso"),
            listOf(FILL_LIGHT_GREY, FILL_LIGHT_GREY, FILL_DARK_GREY),
            if (isGlider())
                listOf(bundle.getString("cruiseMod"), bundle.getString("halfCruise"), bundle.getString("protoDestroyed"))
            else
                listOf(bundle.getString("jumpMod"), bundle.getString("halfJump"), bundle.getString("protoDestroyed")), g)
        ypos += lineHeight
        if (!isQuad()) {
            addHitTableRow(
                colXOffsets, ypos, kern, "10", bundle.getString("leftArm"),
                listOf(FILL_WHITE, FILL_LIGHT_GREY),
                listOf(bundle.getString("toHitMod"), bundle.getString("leftArmDestroyed")), g
            )
            ypos += lineHeight
        }
        addHitTableRow(
            colXOffsets, ypos, kern, "12", bundle.getString("head"),
            listOf(FILL_WHITE, FILL_LIGHT_GREY),
            listOf(bundle.getString("toHitMod"), bundle.getString("sensorsDestroyed")), g
        )
        ypos = inner.bottomY() - fontHeight * if (isQuad()) 3 else 2
        addTextElement(inner.x, ypos, bundle.getString("torsoWeaponDestroyed"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += fontHeight
        addTextElement(inner.x, ypos, "", FONT_SIZE_VSMALL,
            SVGConstants.SVG_BOLD_VALUE, id = "torsoWeapon_0", width = inner.width * 0.3, parent = g)
        addTextElement(inner.x + inner.width * 0.33, ypos, "", FONT_SIZE_VSMALL,
            SVGConstants.SVG_BOLD_VALUE, id = "torsoWeapon_1", width = inner.width * 0.3, parent = g)
        addTextElement(inner.x + inner.width * 0.66, ypos, "", FONT_SIZE_VSMALL,
            SVGConstants.SVG_BOLD_VALUE, id = "torsoWeapon_2", width = inner.width * 0.3, parent = g)
        if (isQuad()) {
            ypos += fontHeight
            addTextElement(
                inner.x, ypos, "", FONT_SIZE_VSMALL,
                SVGConstants.SVG_BOLD_VALUE, id = "torsoWeapon_3", width = inner.width * 0.3, parent = g
            )
            addTextElement(
                inner.x + inner.width * 0.33, ypos, "", FONT_SIZE_VSMALL,
                SVGConstants.SVG_BOLD_VALUE, id = "torsoWeapon_4", width = inner.width * 0.3, parent = g
            )
            addTextElement(
                inner.x + inner.width * 0.66, ypos, "", FONT_SIZE_VSMALL,
                SVGConstants.SVG_BOLD_VALUE, id = "torsoWeapon_5", width = inner.width * 0.3, parent = g
            )
        }
        document.documentElement.appendChild(g)
    }

    fun addHitTableRow(colX: List<Double>, ypos: Double, kern: Double, roll: String, locName: String,
            boxFill: List<String?>, critNames: List<String>, parent: Element) {
        val fontSize = FONT_SIZE_VSMALL
        val lineHeight = calcFontHeight(fontSize).toDouble()
        for (i in roll.split("|").withIndex()) {
            addTextElement(
                colX[0], ypos + lineHeight * i.index, i.value, fontSize, SVGConstants.SVG_BOLD_VALUE,
                anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
        }
        addTextElement(colX[1], ypos, locName, fontSize, SVGConstants.SVG_BOLD_VALUE,
            parent = parent)
        for (i in critNames.withIndex()) {
            var xpos = colX[i.index + 2]
            if (i.index < boxFill.size) {
                boxFill[i.index] ?.let {
                    val box = RoundedBorder(
                        xpos, ypos - lineHeight * 0.8, lineHeight, lineHeight,
                        1.315, 0.726,
                        0.96, fill = it)
                    parent.appendChild(box.draw(document))
                    xpos += lineHeight * 1.2
                }
            }
            val lines = i.value.split("|")
            for (line in lines.withIndex()) {
                addTextElement(
                    xpos, ypos + line.index * lineHeight, line.value, fontSize,
                    SVGConstants.SVG_BOLD_VALUE, fixedWidth = true,
                    width = calcTextLength(line.value, fontSize, SVGConstants.SVG_BOLD_VALUE) * kern,
                    parent = parent)
            }
        }
    }

    fun addArmorDiagram(rect: Cell) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val label = RSLabel(
            this, rect.width * 0.5, 0.0, bundle.getString("armorPanel.title"),
            FONT_SIZE_FREE_LABEL, center = true
        )
        g.appendChild(label.draw())
        embedImage(
            0.0, label.height(), rect.width, rect.height - label.height() - PADDING,
            armorDiagramFileName, ImageAnchor.CENTER, g
        )
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
    }

    fun addPilotData(rect: Cell, tabWidth: Double) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val inner = addBorder(
            0.0, 0.0, rect.width - PADDING, rect.height,
            bundle.getString("pilotPanel.title"),
            fontSize = FONT_SIZE_FREE_LABEL, tabWidth = tabWidth,
            strokeWidth = INNER_STROKE_WIDTH, dropShadow = false,
            equalBevels = true, parent = g
        )
        val fontSize = FONT_SIZE_MEDIUM
        val lineHeight = inner.height / 2.0
        addField(
            bundle.getString("name"), "pilotName0",
            PADDING + bevelX, inner.y + lineHeight * 0.8, fontSize,
            blankId = "blankCrewName0",
            blankWidth = inner.width * 0.45 - PADDING * 2 - bevelX
                    - calcTextLength(
                "${bundle.getString("name")}_",
                fontSize, SVGConstants.SVG_BOLD_VALUE
            ),
            labelFixedWidth = false, parent = g
        )
        addField(
            bundle.getString("gunnerySkill"), "gunnerySkill0",
            PADDING + bevelX,
            inner.y + lineHeight * 1.8, fontSize, defaultText = "0",
            blankId = "blankGunnerySkill0", labelId = "gunnerySkillText0",
            blankWidth = inner.width * 0.13, parent = g
        )
        addPilotDamageTrack(
            inner.x + inner.width * 0.45, inner.y + (inner.height - bevelY - 16) * 0.5,
            inner.width * 0.5, parent = g
        )
        document.documentElement.appendChild(g)
    }

    private fun addPilotDamageTrack(x: Double, y: Double, width: Double, parent: Element) {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        val chartBounds = Cell(
            x + width * 0.35, y,
            width * 0.65 - PADDING, 18.0
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
            chartBounds.x - PADDING, starty, bundle.getString("hitsTaken"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - PADDING, parent = g
        )
        addTextElement(
            chartBounds.x - PADDING, starty + chartBounds.height / 2.0, bundle.getString("consciousnessNum"),
            5.2f, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_END_VALUE,
            width = chartBounds.x - x - PADDING, parent = g
        )
        parent.appendChild(g)
    }
}

internal class BipedProtomechRecordSheet(size: PaperSize, color: Boolean):
    ProtomechRecordSheet(size, color) {
    override val fileName = "protomech_biped.svg"
    override val armorDiagramFileName = "armor_diagram_protomech_biped.svg"
    override fun isQuad() = false
    override fun isGlider() = false
    override fun locations() = 6
}

internal class QuadProtomechRecordSheet(size: PaperSize, color: Boolean):
    ProtomechRecordSheet(size, color) {
    override val fileName = "protomech_quad.svg"
    override val armorDiagramFileName = "armor_diagram_protomech_quad.svg"
    override fun isQuad() = true
    override fun isGlider() = false
    override fun locations() = 4
}

internal class GliderProtomechRecordSheet(size: PaperSize, color: Boolean):
        ProtomechRecordSheet(size, color) {
    override val fileName = "protomech_glider.svg"
    override val armorDiagramFileName = "armor_diagram_protomech_biped.svg"
    override fun isQuad() = false
    override fun isGlider() = true
    override fun locations() = 7

    override fun addMPFields(x: Double, y: Double, fontSize: Float, parent: Element) {
        addFieldSet(listOf(
            LabeledField(bundle.getString("ground"), "mpGround", "1"),
            LabeledField(bundle.getString("cruise"), "mpWalk", "0"),
            LabeledField(bundle.getString("flank"), "mpRun", "0")
        ), x, y, fontSize, FILL_DARK_GREY, 50.0,
            SVGConstants.SVG_MIDDLE_VALUE, labelFixedWidth = false, parent = parent)
    }
}