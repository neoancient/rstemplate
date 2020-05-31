package org.megamek.rstemplate.layout

import org.apache.batik.util.SVGConstants
import org.megamek.rstemplate.templates.CGL_LOGO
import org.megamek.rstemplate.templates.CGL_LOGO_BW
import org.megamek.rstemplate.templates.FONT_SIZE_SMALL
import org.megamek.rstemplate.templates.RecordSheet
import org.w3c.dom.Element
import java.util.*

/**
 *
 */
class TransportLocTable(private val sheet: RecordSheet) {
    private val bundle: ResourceBundle = ResourceBundle.getBundle(TransportLocTable::class.java.name)

    private val stdLocations = listOf(
        listOf("1", bundle.getString("rt"), bundle.getString("rs")),
        listOf("2", bundle.getString("lt"), bundle.getString("rs")),
        listOf("3", bundle.getString("rtr"), bundle.getString("ls")),
        listOf("4", bundle.getString("ltr"), bundle.getString("ls")),
        listOf("5", bundle.getString("ctr"), bundle.getString("rr")),
        listOf("6", bundle.getString("ct"), bundle.getString("rr"))
    )
    private val lsvLocations = listOf(
        listOf("1", bundle.getString("rs2")),
        listOf("2", bundle.getString("rs2")),
        listOf("3", bundle.getString("ls2")),
        listOf("4", bundle.getString("ls2")),
        listOf("5", bundle.getString("rr2")),
        listOf("6", bundle.getString("rr2"))
    )

    fun draw(rect: Cell, parent: Element = sheet.rootElement) {
        val g = sheet.createTranslatedGroup(rect.x, rect.y)
        val inner = sheet.addBorder(0.0, 0.0, rect.width, rect.height,
            bundle.getString("transportLoc.title"), topTab = false, bottomTab = false,
            parent = g)
        val lineHeight = inner.height / (stdLocations.size + lsvLocations.size + 8)
        var ypos = inner.y + lineHeight
        sheet.addTextElement(inner.width * 0.12, ypos, bundle.getString("trooper"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.45, ypos, bundle.getString("mech"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.86, ypos, bundle.getString("vehicle"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_SMALL,
            stdLocations, listOf(0.12, 0.45, 0.86),
            listOf(bundle.getString("number"), bundle.getString("location"), bundle.getString("location")),
            firstColAnchor = SVGConstants.SVG_MIDDLE_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)
        ypos += lineHeight

        val imgWidth = rect.width * 0.25 - padding
        sheet.embedImage(inner.x + inner.width * 0.86 - imgWidth * 0.5, ypos, imgWidth, inner.height - ypos,
            CGL_LOGO, CGL_LOGO_BW, ImageAnchor.CENTER, id = "cglLogo", parent = g)

        sheet.addTextElement(inner.width * 0.12, ypos, bundle.getString("trooper"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        sheet.addTextElement(inner.width * 0.45, ypos, bundle.getString("largeSupport"),
            FONT_SIZE_SMALL, SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        ypos += lineHeight
        ypos += sheet.createTable(0.0, ypos, inner.width, FONT_SIZE_SMALL,
            lsvLocations, listOf(0.12, 0.45),
            listOf(bundle.getString("number"), bundle.getString("vehicleLocation")),
            firstColAnchor = SVGConstants.SVG_MIDDLE_VALUE,
            firstColBold = false, lineHeight = lineHeight, parent = g)
        ypos += lineHeight
        sheet.addTextElement(inner.width * 0.04, ypos, bundle.getString("lsvNote"),
            FONT_SIZE_SMALL, parent = g)

        parent.appendChild(g)
    }
}