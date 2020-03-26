package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.FONT_SIZE_VSMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.megamek.rstemplate.templates.truncate
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class InfantryBurstFireTable(private val sheet: RecordSheet) {

    private val bundle: ResourceBundle = ResourceBundle.getBundle(InfantryBurstFireTable::class.java.name)

    private val mechDamage = listOf(
        listOf(bundle.getString("apGauss"), "2D6"),
        listOf(bundle.getString("lightMG"), "1D6"),
        listOf(bundle.getString("MG"), "2D6"),
        listOf(bundle.getString("heavyMG"), "3D6"),
        listOf(bundle.getString("smallPulse"), "2D6"),
        listOf(bundle.getString("flamer"), "4D6")
    )
    private val baDamage = listOf(
        listOf(bundle.getString("lightMG"), bundle.getString("halfD6")),
        listOf(bundle.getString("MG"), "1D6"),
        listOf(bundle.getString("heavyMG"), "2D6"),
        listOf(bundle.getString("flamer"), "3D6"),
        listOf(bundle.getString("lightRecoilless"), "1D6"),
        listOf(bundle.getString("mediumRecoilless"), "2D6"),
        listOf(bundle.getString("heavyRecoilless"), "2D6"),
        listOf(bundle.getString("lightMortar"), "1D6"),
        listOf(bundle.getString("heavyMortar"), "1D6"),
        listOf(bundle.getString("autoGrenade"), bundle.getString("halfD6")),
        listOf(bundle.getString("heavyGrenade"), "1D6")
    )

    fun draw(rect: Cell, parent: Element = sheet.document.documentElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("burstTable.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / (mechDamage.size + baDamage.size + 8)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.04, ypos, bundle.getString("mechsProtosVees"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += lineHeight * 2
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            mechDamage, listOf(0.04, 0.75),
            listOf(bundle.getString("weapon"),
                bundle.getString("damage")), firstColAnchor = SVGConstants.SVG_START_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)
        ypos += lineHeight

        sheet.addTextElement(inner.width * 0.04, ypos, bundle.getString("battleArmor"),
            FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE, parent = g)
        ypos += lineHeight * 2
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_VSMALL,
            baDamage, listOf(0.04, 0.75),
            listOf(bundle.getString("weapon"),
                bundle.getString("damage")), firstColAnchor = SVGConstants.SVG_START_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)

        parent.appendChild(g)
    }
}
