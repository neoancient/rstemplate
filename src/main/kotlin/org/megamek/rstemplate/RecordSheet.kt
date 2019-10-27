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
import org.megamek.rstemplate.layout.PaperSize
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
const val LEFT_MARGIN = 72
const val RIGHT_MARGIN = 72
const val TOP_MARGIN = 36
const val BOTTOM_MARGIN = 36
const val TYPEFACE = "Eurostile"
const val FILL_BLACK = "#000000"
const val FILL_LIGHT_GREY = "#c8c7c7"
const val FILL_DARK_GREY = "#231f20"
const val FONT_SIZE_LARGE = 7.2f
const val FONT_SIZE_MEDIUM = 6.76f
const val FONT_SIZE_SMALL = 6.2f
const val FONT_SIZE_VSMALL = 5.8f


const val svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI

data class RecordSheet(val size: PaperSize) {

    val document = generate()
    val svgGenerator = SVGGraphics2D(document)
    val font= Font.decode(TYPEFACE) ?: Font.decode(Font.SANS_SERIF) ?: Font.decode(null)
    val logoHeight = addLogo()
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

        return Pair(dim.x * scale, dim.y * scale)
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
        return fm.getHeight().toFloat()
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
    fun addLogo() = embedImage(0.0, 0.0, width() * 0.5, null, "btlogo.svg").second

    /**
     * Adds the copyright footer.
     *
     * @return The height of the copyright footer's text element.
     */
    fun addCopyrightFooter(): Double {
        val bundle = ResourceBundle.getBundle(this::class.java.name)
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
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "-${height * 2.0}")
        tspan.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE, width().toString())
        tspan.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
        tspan.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "tspanCopyright")
        tspan.textContent = line1
        textElem.appendChild(tspan)

        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, "0.0")
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "-$height")
        tspan.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE,
            (width() * calcTextLength(line2, FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE)
                    / calcTextLength(line1, FONT_SIZE_VSMALL, SVGConstants.SVG_BOLD_VALUE)).toString())
        tspan.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
        tspan.textContent = line2
        textElem.appendChild(tspan)

        document.documentElement.appendChild(textElem)
        return height * 2.0
    }
}