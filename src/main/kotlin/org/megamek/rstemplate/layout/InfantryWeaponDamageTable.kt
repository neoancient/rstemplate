package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.*
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class InfantryWeaponDamageTable(val sheet: RecordSheet) {

    private val bundle: ResourceBundle = ResourceBundle.getBundle(InfantryWeaponDamageTable::class.java.name)

    private val damage = listOf(
        listOf(bundle.getString("directFire"), "${bundle.getString("damageValue")} / 10"),
        listOf(bundle.getString("clusterBallistic"), "${bundle.getString("damageValue")} / 10 + 1"),
        listOf(bundle.getString("pulse"), "${bundle.getString("damageValue")} / 10 + 2"),
        listOf(bundle.getString("clusterMissile"), "${bundle.getString("damageValue")} / 5"),
        listOf(bundle.getString("areaEffect"), "${bundle.getString("damageValue")} / 5"),
        listOf(bundle.getString("burstFire"), bundle.getString("seeBurstFire")),
        listOf(bundle.getString("heatEffect"), bundle.getString("seeHeatEffect"))
    )

    fun draw(rect: Cell, parent: Element = sheet.document.documentElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("infantryWeaponDamage.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height/ (damage.size + 14)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.75, ypos, bundle.getString("numberHit.1"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE,
            parent = g)
        ypos += lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            damage, listOf(0.04, 0.75),
            listOf(bundle.getString("weaponType"),
                bundle.getString("numberHit.2")), firstColAnchor = SVGConstants.SVG_START_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)

        for (i in 1..4) {
            ypos += sheet.addParagraph(inner.x + padding, ypos,
                (if (i == 4) inner.width * 0.75 else inner.width) - padding, bundle.getString("notes.$i"),
                FONT_SIZE_VSMALL, g)
        }

        sheet.embedImage(inner.rightX() - padding * 2 - CGL_LOGO_WIDTH,
            inner.bottomY() - padding * 2 - CGL_LOGO_HEIGHT,
            CGL_LOGO_WIDTH, CGL_LOGO_HEIGHT, if (sheet.color) CGL_LOGO else CGL_LOGO_BW,
            ImageAnchor.BOTTOM_RIGHT, g)

        parent.appendChild(g)
    }
}