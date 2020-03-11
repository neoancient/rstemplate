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
        addDamagePanelTextFields(padding, height * 0.5, width - padding * 2.0, height * 0.5, g)
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

    private fun addDamagePanelTextFields(x: Double, y: Double, width: Double, height: Double, parent: Element) {
        val lineHeight = height / 7.0
        var ypos = height + lineHeight
        addTextElement(x, ypos, bundle.getString("damageGrouping"), FONT_SIZE_VSMALL, parent = parent)
        addTextElement(x + width * 0.5, ypos, bundle.getString("rangeInHexes"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, parent = parent)
        ypos += lineHeight
        addTextElement(x, ypos, bundle.getString("range"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, parent = parent)
        val xpos = x + width * 0.14
        for (range in 0..21) {
            addTextElement(xpos + range * width * 0.04, ypos, range.toString(), FONT_SIZE_SMALL,
                SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = parent)
            addTextElement(xpos + range * width * 0.04, ypos + lineHeight, "\u2013",
                FONT_SIZE_SMALL, anchor = SVGConstants.SVG_MIDDLE_VALUE, id = "range_mod_$range",
                parent = parent)
            addTextElement(xpos + range * width * 0.04, ypos + lineHeight * 2, "\u2013",
                FONT_SIZE_SMALL, anchor = SVGConstants.SVG_MIDDLE_VALUE, id = "uw_range_mod_$range", hidden = true,
                parent = parent)
        }
        ypos += lineHeight
        addTextElement(x, ypos, bundle.getString("rangeModifier"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, parent = parent)
        ypos += lineHeight
        addTextElement(x, ypos, bundle.getString("underwater"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, parent = parent)
        ypos += lineHeight
        val fieldGunGroup = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        fieldGunGroup.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "field_gun_columns")
        parent.appendChild(fieldGunGroup)
        addTextElement(x, ypos, bundle.getString("quantity"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, parent = fieldGunGroup)
        addTextElement(x + width * 0.04, ypos, bundle.getString("fieldGunType"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.19, ypos, bundle.getString("damage"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.27, ypos, bundle.getString("minimum"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.30, ypos, bundle.getString("short"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.33, ypos, bundle.getString("medium"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.36, ypos, bundle.getString("long"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.4, ypos, bundle.getString("ammo"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        addTextElement(x + width * 0.45, ypos, bundle.getString("crew"), FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, parent = fieldGunGroup)
        parent.appendChild(createDestMods(x + width * 0.5, ypos, lineHeight * 0.8))
        parent.appendChild(createSneakCamoMods(x + width * 0.5, ypos, width, lineHeight * 0.8))
        parent.appendChild(createSneakIRMods(x + width * 0.75, ypos, width, lineHeight * 0.8))
        ypos += lineHeight
        addTextElement(x, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, id = "field_gun_qty", parent = fieldGunGroup)
        addTextElement(x + width * 0.04, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_type", parent = fieldGunGroup)
        addTextElement(x + width * 0.19, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_dmg", parent = fieldGunGroup)
        addTextElement(x + width * 0.27, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_min_range", parent = fieldGunGroup)
        addTextElement(x + width * 0.30, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_short", parent = fieldGunGroup)
        addTextElement(x + width * 0.33, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_med", parent = fieldGunGroup)
        addTextElement(x + width * 0.36, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_long", parent = fieldGunGroup)
        addTextElement(x + width * 0.4, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_ammo", parent = fieldGunGroup)
        addTextElement(x + width * 0.45, ypos, "", FONT_SIZE_SMALL,
            SVGConstants.SVG_BOLD_VALUE, hidden = true, id = "field_gun_crew", parent = fieldGunGroup)
        ypos += lineHeight
    }

    private fun createDestMods(x: Double, y: Double, lineHeight: Double): Element {
        val destMods = createTextElement(0.0, 0.0, "", FONT_SIZE_SMALL, id = "dest_mods")
        var tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        tspan.textContent = bundle.getString("dest_mods_1")
        destMods.appendChild(tspan)
        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, (y + lineHeight).truncate())
        tspan.textContent = bundle.getString("dest_mods_2")
        destMods.appendChild(tspan)
        return destMods
    }

    private fun createSneakCamoMods(x: Double, y: Double, width: Double, lineHeight: Double): Element {
        val mods = createTextElement(0.0, 0.0, "", FONT_SIZE_SMALL, id="sneak_camo_mods")
        var tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        tspan.textContent = bundle.getString("mp_used")
        mods.appendChild(tspan)
        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, (y + lineHeight).truncate())
        tspan.textContent = bundle.getString("toHitMod")
        mods.appendChild(tspan)
        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, (y + lineHeight * 2).truncate())
        tspan.textContent = bundle.getString("allAttackers")
        mods.appendChild(tspan)
        mods.appendChild(centerTspan(x + width * 0.11, y, "0", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.14, y, "1", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.17, y, "2", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.2, y, "3+", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.11, y + lineHeight, "+3", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.14, y + lineHeight, "+2", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.17, y + lineHeight, "+1", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.2, y + lineHeight, "0", FONT_SIZE_SMALL))
        return mods
    }

    private fun createSneakIRMods(x: Double, y: Double, width: Double, lineHeight: Double): Element {
        val mods = createTextElement(x, y, "", FONT_SIZE_SMALL, id="sneak_camo_mods")
        var tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        tspan.textContent = bundle.getString("rangeCategory")
        mods.appendChild(tspan)
        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, (y + lineHeight).truncate())
        tspan.textContent = bundle.getString("toHitMod")
        mods.appendChild(tspan)
        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, (y + lineHeight * 2).truncate())
        tspan.textContent = bundle.getString("nonInfantryAttackers")
        mods.appendChild(tspan)
        mods.appendChild(centerTspan(x + width * 0.133, y, bundle.getString("short"), FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.166, y, bundle.getString("medium"), FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.2, y, bundle.getString("long"), FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.133, y + lineHeight, "+1", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.166, y + lineHeight, "+1", FONT_SIZE_SMALL))
        mods.appendChild(centerTspan(x + width * 0.2, y + lineHeight, "+2", FONT_SIZE_SMALL))
        return mods
    }

    private fun centerTspan(x: Double, y: Double, text: String, fontSize: Float): Element {
        val tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE,
            (x - calcTextLength(text, fontSize) * 0.5).truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        tspan.textContent = text
        return tspan
    }
}