package org.megamek.rstemplate

import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.anim.dom.SVGDOMImplementation
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.DocumentLoader
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.svggen.SVGGraphics2D
import org.apache.batik.util.SVGConstants
import org.apache.batik.util.XMLResourceDescriptor
import org.megamek.rstemplate.layout.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.Font
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.math.min

/**
 *
 */

const val SVGNS = SVGDOMImplementation.SVG_NAMESPACE_URI
const val LEFT_MARGIN = 36
const val RIGHT_MARGIN = 36
const val TOP_MARGIN = 36
const val BOTTOM_MARGIN = 36
const val TYPEFACE = "Eurostile"
const val FILL_BLACK = "#000000"
const val FILL_LIGHT_GREY = "#c8c7c7"
const val FILL_DARK_GREY = "#231f20"
const val FILL_WHITE = "#ffffff"
const val FONT_SIZE_TAB_LABEL = 10.6f
const val FONT_SIZE_FREE_LABEL = 8.6f
const val FONT_SIZE_VLARGE = 11.59f
const val FONT_SIZE_LARGE = 7.2f
const val FONT_SIZE_MEDIUM = 6.76f
const val FONT_SIZE_SMALL = 6.2f
const val FONT_SIZE_VSMALL = 5.8f


const val svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI

open class RecordSheet(val size: PaperSize) {

    val document = generate()
    val svgGenerator = SVGGraphics2D(document)
    val font= Font.decode(TYPEFACE) ?: Font.decode(Font.SANS_SERIF) ?: Font.decode(null)
    val logoHeight = addLogo()
    val titleHeight = addTitle()
    val footerHeight = addCopyrightFooter()

    /**
     * @return width of printable area
     */
    fun width() = size.width - LEFT_MARGIN - RIGHT_MARGIN

    /**
     * @return height of printable area
     */
    fun height() = size.height - TOP_MARGIN - BOTTOM_MARGIN

    /**
     * Generates the SVG document
     */
    fun generate(): Document {
        val domImpl = SVGDOMImplementation.getDOMImplementation()
        val doc = domImpl.createDocument(SVGNS, SVGConstants.SVG_SVG_TAG, null)
        val svgRoot = doc.getDocumentElement()
        svgRoot.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, size.width.toString())
        svgRoot.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, size.height.toString())

        return doc
    }

    /**
     * Loads an SVG document from a resource and embeds it in a another document, optionally scaling it to fit.
     * Scaling always maintains the aspect ratio and will size the embedded image. If both height and width
     * parameters are provided, the image will be scaled to match the more restrictive.
     *
     * @param x         The x coordinate of the intended position in the parent document
     * @param y         The y coordinate of the intended position in the parent document
     * @param w         The maximum width of the image in the parent document. If x is {@code null}, any scaling will not take
     *                  width into account.
     * @param h         The maximum height of the image in the parent document. If y is {@code null}, any scaling will not take
     *                  height into account.
     * @param name      The name of the resource file relative to the current class.
     */
    fun embedImage(x: Double = 0.0, y: Double = 0.0, w: Double? = null, h: Double? = null, name: String): Pair<Double, Double> {
        val istr = this::class.java.getResourceAsStream(name)
        if (istr == null) {
            return Pair(0.0, 0.0)
        }
        val factory = SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName())
        val imageDoc = factory.createDocument(this::class.java.getResource(name).toString(), istr)
        val agent = UserAgentAdapter()
        val bridgeContext = BridgeContext(agent, DocumentLoader(agent))

        val dim = GVTBuilder().build(bridgeContext, imageDoc).primitiveBounds
        var scale = 1.0
        if (h != null && w != null) {
            scale = min(w / dim.width, h / dim.height)
        } else if (h != null) {
            scale = h / dim.height
        } else if (w != null) {
            scale = w / dim.width
        }

        val gElement = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        gElement.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.TRANSFORM_MATRIX}($scale 0 0 $scale ${x + LEFT_MARGIN} ${y + TOP_MARGIN})");

        for (i in 0 until imageDoc.documentElement.childNodes.length) {
            val node = imageDoc.documentElement.childNodes.item(i)
            gElement.appendChild(document.importNode(node, true))
        }
        document.documentElement.appendChild(gElement)

        return Pair(dim.width * scale, dim.height * scale)
    }

    protected fun addTextElement(parent: Element, x: Double, y: Double, text: String,
                                 fontSize: Float, anchor: String = SVGConstants.SVG_START_VALUE,
                                 weight: String = SVGConstants.SVG_NORMAL_VALUE,
                                 fill: String = "#000000"): Double {
        val newText = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        newText.setTextContent(text)
        newText.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.toString())
        newText.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.toString())
        newText.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, font.name)
        newText.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, fontSize.toString() + "px")
        newText.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, weight)
        newText.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
        newText.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fill)
        parent.appendChild(newText)

        return calcTextLength(text, fontSize, weight)
    }

    /**
     * Determines the vertical space taken up by a line of text.
     *
     * @param fontSize  Value of CSS font-size attribute
     * @return          The height of the bounding box of a text element
     */
    fun calcFontHeight(fontSize: Float): Float {
        val f = font.deriveFont(fontSize)
        val fm = svgGenerator.getFontMetrics(f)
        return fm.height.toFloat()
    }

    fun calcTextLength(text: String, fontSize: Float, fontWeight: String = SVGConstants.SVG_NORMAL_VALUE): Double {
        val font = font.deriveFont(if (fontWeight == SVGConstants.SVG_BOLD_VALUE) Font.BOLD else Font.PLAIN, fontSize)
        return font.getStringBounds(text, svgGenerator.getFontRenderContext()).getWidth()
    }

    /**
     * Export the SVG document to a stream
     *
     * @param ostream The stream to write the document to
     */
    fun export(ostream: OutputStream = System.out) {
        val writer = OutputStreamWriter(ostream, "UTF-8")
        svgGenerator.stream(document.documentElement, writer)
    }

    /**
     * Places the BT logo in the top left corner of the page, sized to take half the width of the page.
     *
     * @return The height of the logo after scaling
     */
    fun addLogo() = embedImage(0.0, 0.0, width() * 0.67, null, "btlogo.svg").second

    /**
     * Places a generic title under the BT logo
     *
     * @return The height of the title text
     */
    fun addTitle(): Double {
        val height = calcFontHeight(FONT_SIZE_VLARGE).toDouble()
        val textElem = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        textElem.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${LEFT_MARGIN + width() / 3.0} ${TOP_MARGIN + logoHeight + height})")
        textElem.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "title")
        textElem.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, font.name)
        textElem.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "${FONT_SIZE_VLARGE}px")
        textElem.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, SVGConstants.SVG_BOLD_VALUE)
        textElem.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE)
        textElem.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, FILL_BLACK)
        textElem.textContent = "RECORD SHEET"
        document.documentElement.appendChild(textElem)
        return height
    }

    /**
     * Adds the copyright footer.
     *
     * @return The height of the copyright footer's text element.
     */
    fun addCopyrightFooter(): Double {
        val bundle = ResourceBundle.getBundle(RecordSheet::class.java.name)
        val height = calcFontHeight(FONT_SIZE_VSMALL)
        val line1 = bundle.getString("copyright.line1.text")
        val line2 = bundle.getString("copyright.line2.text")

        val textElem = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        textElem.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${size.width / 2.0} ${size.height - BOTTOM_MARGIN})")
        textElem.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, font.name)
        textElem.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "${FONT_SIZE_VSMALL}px")
        textElem.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, SVGConstants.SVG_BOLD_VALUE)
        textElem.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE)
        textElem.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, FILL_DARK_GREY)

        var tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, "0.0")
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "-$height")
        tspan.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE, (width() * 0.95).toString())
        tspan.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
        tspan.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "tspanCopyright")
        tspan.textContent = line1
        textElem.appendChild(tspan)

        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, "0.0")
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "0.0")
        tspan.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE, (width() * 0.9).toString())
        tspan.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
        tspan.textContent = line2
        textElem.appendChild(tspan)

        document.documentElement.appendChild(textElem)
        return height * 2.0
    }

    /**
     * Draws a titled border around an area.
     *
     * @param x The x coordinate of the upper left
     * @param y The y coordinate of the upper left
     * @param width The width of the region
     * @param height The height of the region
     * @param title The title text
     * @param bottomTab Whether to add a tab on the bottom right like the one at the top
     * @param bevelTopRight Whether to bevel the top right corner
     * @param bevelBottomLeft Whether to bevel the bottom left corner
     * @param bevelBottomRight Whether to bevel the bottom right corner; ignored if {@code bottomTab} is true
     * @return The y coordinate of the bottom of the title label
     */
    fun addBorder(x: Double, y: Double, width: Double, height: Double, title: String,
                  bottomTab: Boolean = false,
                  bevelTopRight: Boolean = true, bevelBottomRight: Boolean = true,
                  bevelBottomLeft: Boolean = true): Double {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} ($x,$y)")

        val label = RSLabel(this,2.5, 3.0, title, FONT_SIZE_TAB_LABEL)
        val shadow = CellBorder(2.5, 2.5, width - 6.0, height - 6.0,
            label.rectWidth + 4, FILL_LIGHT_GREY, 3.0, bottomTab, bevelTopRight, bevelBottomRight, bevelBottomLeft)
        val border = CellBorder(0.0, 0.0, width - 5.0, height - 5.0,
            label.rectWidth + 4, FILL_DARK_GREY, 1.932, bottomTab, bevelTopRight, bevelBottomRight, bevelBottomLeft)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        g.appendChild(label.draw())
        document.documentElement.appendChild(g)
        return 3.0 + label.textHeight * 2
    }

    fun addTextElement(x: Double, y: Double, text: String, fontSize: Float,
                       fontWeight: String = SVGConstants.SVG_NORMAL_VALUE,
                       fill: String = FILL_BLACK, anchor: String = SVGConstants.SVG_START_VALUE,
                       id: String? = null,
                       fixedWidth: Boolean = false,
                       width: Double? = null) {
        val element = createTextElement(x, y, text, fontSize, fontWeight, fill, anchor,
            id, fixedWidth, width)
        document.documentElement.appendChild(element)
    }

    fun createTextElement(x: Double, y: Double, text: String, fontSize: Float,
                       fontWeight: String = SVGConstants.SVG_NORMAL_VALUE,
                       fill: String = FILL_BLACK, anchor: String = SVGConstants.SVG_START_VALUE,
                       id: String? = null,
                       fixedWidth: Boolean = false, width: Double? = null): Element {
        val t = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        t.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.toString())
        t.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.toString())
        t.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, TYPEFACE)
        t.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, fontSize.toString())
        t.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, fontWeight)
        t.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fill)
        t.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
        if (fixedWidth) {
            t.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
            t.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE,
                (width ?: calcTextLength(text, fontSize, fontWeight)).toString())
        }
        if (id != null) {
            t.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        }
        t.textContent = text

        return t
    }

    fun addField(label: String, id: String, x: Double, y: Double,
                 fontSize: Float, fontWeight: String = SVGConstants.SVG_BOLD_VALUE) {
        addFieldSet(listOf(Pair(label, id)), x, y, fontSize, fontWeight)
    }

    fun addFieldSet(fields: List<Pair<String, String>>, x: Double, y: Double,
                    fontSize: Float, fontWeight: String = SVGConstants.SVG_BOLD_VALUE) {
        val labelWidth = fields.map{calcTextLength("${it.first}_", fontSize, fontWeight)}.max() ?: 0.0
        val lineHeight = calcFontHeight(fontSize).toDouble()
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} ($x,$y)")
        for (field in fields.withIndex()) {
            g.appendChild(createTextElement(0.0, lineHeight * field.index,
                field.value.first, fontSize, fontWeight, fixedWidth = true))
            g.appendChild(createTextElement(labelWidth, lineHeight * field.index,
                "[${field.value.second}]", fontSize, fontWeight, id = field.value.second, fixedWidth = false))
        }
        document.documentElement.appendChild(g)
    }

    fun addHorizontalLine(x: Double, y: Double, width: Double, strokeWidth: Double = STROKE_WIDTH,
                          stroke: String = FILL_BLACK) {
        val path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M $x,$y L ${x + width},$y")
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, stroke)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.toString())
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        document.documentElement.appendChild(path)
    }
}