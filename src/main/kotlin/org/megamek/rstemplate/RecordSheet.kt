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
import java.lang.String.format
import java.util.*
import kotlin.math.min
import kotlin.math.sqrt

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
const val FILL_GREEN = "#ddfeeb"
const val FILL_YELLOW = "#fffdd4"
const val FILL_RED = "#fccbce"
const val FILL_ORANGE = "#ffe6cc"
const val FONT_SIZE_TAB_LABEL = 10.6f
const val FONT_SIZE_FREE_LABEL = 8.6f
const val FONT_SIZE_VLARGE = 11.59f
const val FONT_SIZE_LARGE = 7.2f
const val FONT_SIZE_MEDIUM = 6.76f
const val FONT_SIZE_SMALL = 6.2f
const val FONT_SIZE_VSMALL = 5.7f
const val BT_LOGO = "btlogo.svg"
const val CGL_LOGO = "cgllogo.svg"

const val svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI
val crewSizeId = listOf("Single", "Dual", "Triple")
fun Double.truncate() = format("%.3f", this)
fun Float.truncate() = format("%.3f", this)

abstract class RecordSheet(val size: PaperSize) {
    abstract val fileName: String

    val document = generate()
    val svgGenerator = SVGGraphics2D(document)
    val font= Font.decode(TYPEFACE) ?: Font.decode(Font.SANS_SERIF) ?: Font.decode(null)
    val logoHeight = addLogo()
    val titleHeight = addTitle()
    val footerHeight = addCopyrightFooter()

    private val bundle = ResourceBundle.getBundle(RecordSheet::class.java.name)

    /**
     * @return width of printable area
     */
    fun width() = size.width - LEFT_MARGIN - RIGHT_MARGIN

    /**
     * @return height of printable area
     */
    fun height() = size.height - TOP_MARGIN - BOTTOM_MARGIN

    /**
     * Checks for an effect at the given heat level
     *
     * @param heatLevel The unit's heat level
     * @return A description of the heat effect at the given heat level, or {@code null} if there is
     *         none that applies
     */
    open fun heatEffect(heatLevel: Int): String? = null

    /**
     * Generates the SVG document
     */
    fun generate(): Document {
        val domImpl = SVGDOMImplementation.getDOMImplementation()
        val doc = domImpl.createDocument(SVGNS, SVGConstants.SVG_SVG_TAG, null)
        val svgRoot = doc.documentElement
        svgRoot.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, size.width.toString())
        svgRoot.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, size.height.toString())

        return doc
    }

    abstract fun build()

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
     * @param anchor    How to justify the image within the available space.
     * @param parent    The parent element for the image
     * @return          An array with the final width and height of the image after scaling
     *                  and the scale factor, in that order.
     */
    fun embedImage(x: Double = 0.0, y: Double = 0.0, w: Double? = null, h: Double? = null, name: String,
                   anchor: ImageAnchor = ImageAnchor.TOP_LEFT,
                   parent: Element = document.documentElement): Array<Double> {
        val istr = this::class.java.getResourceAsStream(name)
        if (istr == null) {
            return arrayOf(0.0, 0.0, 0.0)
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

        val xpos = if (w != null) x + anchor.xOffset(w, dim.width * scale) else x
        val ypos = if (h != null) y + anchor.yOffset(h, dim.height * scale) else y
        val gElement = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        gElement.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.TRANSFORM_MATRIX}(${scale.truncate()} 0 0 ${scale.truncate()} ${xpos.truncate()} ${ypos.truncate()})")

        for (i in 0 until imageDoc.documentElement.childNodes.length) {
            val node = imageDoc.documentElement.childNodes.item(i)
            gElement.appendChild(document.importNode(node, true))
        }
        parent.appendChild(gElement)

        return arrayOf(dim.width * scale, dim.height * scale, scale)
    }

    protected fun addTextElement(parent: Element, x: Double, y: Double, text: String,
                                 fontSize: Float, anchor: String = SVGConstants.SVG_START_VALUE,
                                 weight: String = SVGConstants.SVG_NORMAL_VALUE,
                                 fill: String = "#000000", maxWidth: Double? = null): Double {
        val newText = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        newText.setTextContent(text)
        newText.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        newText.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        newText.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, font.name)
        newText.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, fontSize.truncate() + "px")
        newText.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, weight)
        newText.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
        newText.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fill)
        if (maxWidth != null && calcTextLength(text, fontSize, weight) > maxWidth) {
            newText.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE,
                SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
            newText.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE, maxWidth.truncate())
        }
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
    fun addLogo() = embedImage(LEFT_MARGIN.toDouble(), TOP_MARGIN.toDouble(), width() * 0.67, null, BT_LOGO)[1]

    /**
     * Places a generic title under the BT logo
     *
     * @return The height of the title text
     */
    fun addTitle(parent: Element = document.documentElement): Double {
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
        parent.appendChild(textElem)
        return height * 1.5
    }

    /**
     * Adds the copyright footer.
     *
     * @return The height of the copyright footer's text element.
     */
    fun addCopyrightFooter(parent: Element = document.documentElement): Double {
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
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, "0")
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "-${height.truncate()}")
        tspan.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE, (width() * 0.95).truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
        tspan.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, "tspanCopyright")
        tspan.textContent = line1
        textElem.appendChild(tspan)

        tspan = document.createElementNS(svgNS, SVGConstants.SVG_TSPAN_TAG)
        tspan.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, "0")
        tspan.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "0")
        tspan.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE, (width() * 0.9).truncate())
        tspan.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
        tspan.textContent = line2
        textElem.appendChild(tspan)

        parent.appendChild(textElem)
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
     * @return The area inside the border
     */
    fun addBorder(x: Double, y: Double, width: Double, height: Double, title: String,
                  bottomTab: Boolean = false,
                  bevelTopRight: Boolean = true, bevelBottomRight: Boolean = true,
                  bevelBottomLeft: Boolean = true,
                  parent: Element = document.documentElement): Cell {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        if (x != 0.0 && y != 0.0) {
            g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
                "${SVGConstants.SVG_TRANSLATE_VALUE} (${x.truncate()} ${y.truncate()})")
        }

        val label = RSLabel(this,2.5, 3.0, title, FONT_SIZE_TAB_LABEL)
        val shadow = CellBorder(2.5, 2.5, width - 6.0, height - 6.0,
            label.rectWidth + 4, FILL_LIGHT_GREY, 5.2, bottomTab, bevelTopRight, bevelBottomRight, bevelBottomLeft)
        val border = CellBorder(0.0, 0.0, width - 5.0, height - 5.0,
            label.rectWidth + 4, FILL_DARK_GREY, 1.932, bottomTab, bevelTopRight, bevelBottomRight, bevelBottomLeft)
        g.appendChild(shadow.draw(document))
        g.appendChild(border.draw(document))
        g.appendChild(label.draw())
        parent.appendChild(g)
        return Cell(x, y, width, height).inset(3.0, 5.0,3.0 + label.textHeight * 2, 5.0)
    }

    fun addTextElement(x: Double, y: Double, text: String, fontSize: Float,
                       fontWeight: String = SVGConstants.SVG_NORMAL_VALUE,
                       fill: String = FILL_DARK_GREY, anchor: String = SVGConstants.SVG_START_VALUE,
                       id: String? = null, fixedWidth: Boolean = false, hidden: Boolean = false,
                       width: Double? = null,parent: Element = document.documentElement) {
        val element = createTextElement(x, y, text, fontSize, fontWeight, fill, anchor,
            id, fixedWidth, width, hidden)
        parent.appendChild(element)
    }

    fun createTextElement(x: Double, y: Double, text: String, fontSize: Float,
                       fontWeight: String = SVGConstants.SVG_NORMAL_VALUE,
                       fill: String = FILL_BLACK, anchor: String = SVGConstants.SVG_START_VALUE,
                       id: String? = null, fixedWidth: Boolean = false, width: Double? = null,
                       hidden: Boolean = false): Element {
        val t = document.createElementNS(svgNS, SVGConstants.SVG_TEXT_TAG)
        t.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        t.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        t.setAttributeNS(null, SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, TYPEFACE)
        t.setAttributeNS(null, SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, fontSize.truncate())
        t.setAttributeNS(null, SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE, fontWeight)
        t.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fill)
        t.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, anchor)
        if (fixedWidth || width != null) {
            t.setAttributeNS(null, SVGConstants.SVG_LENGTH_ADJUST_ATTRIBUTE, SVGConstants.SVG_SPACING_AND_GLYPHS_VALUE)
            t.setAttributeNS(null, SVGConstants.SVG_TEXT_LENGTH_ATTRIBUTE,
                (width ?: calcTextLength(text, fontSize, fontWeight)).truncate())
        }
        if (id != null) {
            t.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        }
        if (hidden) {
            t.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE)
        }
        t.textContent = text

        return t
    }

    fun addField(label: String, id: String, x: Double, y: Double,
                 fontSize: Float, fill: String = FILL_DARK_GREY, defaultText: String = "Lorem Ipsum",
                 fieldOffset: Double? = null,
                 fieldAnchor: String = SVGConstants.SVG_START_VALUE,
                 labelId: String? = null, blankId: String? = null,
                 blankWidth: Double? = null, labelFixedWidth: Boolean = true, hidden: Boolean = false,
                 parent: Element = document.documentElement) {
        addFieldSet(listOf(LabeledField(label, id, defaultText, labelId, blankId)), x, y, fontSize, fill,
            fieldOffset, fieldAnchor, blankWidth, labelFixedWidth, hidden, parent)
    }

    fun addFieldSet(fields: List<LabeledField>, x: Double, y: Double,
                    fontSize: Float, fill: String = FILL_DARK_GREY,
                    fieldOffset: Double? = null,
                    fieldAnchor: String = SVGConstants.SVG_START_VALUE,
                    blankWidth: Double? = null, labelFixedWidth: Boolean = true, hidden: Boolean = false,
                    parent: Element = document.documentElement) {
        val labelWidth = fieldOffset ?: fields.map{calcTextLength("${it.labelText}_", fontSize, SVGConstants.SVG_BOLD_VALUE)}.max() ?: 0.0
        val lineHeight = calcFontHeight(fontSize).toDouble()
        for (field in fields.withIndex()) {
            field.value.draw(this, x, y + lineHeight * field.index, fontSize, fill,
                x + labelWidth, blankWidth, fieldAnchor = fieldAnchor, labelFixedWidth = labelFixedWidth,
                hidden = hidden, parent = parent)
        }
    }

    fun addHorizontalLine(x: Double, y: Double, width: Double, strokeWidth: Double = STROKE_WIDTH,
                          stroke: String = FILL_BLACK, id: String? = null, hidden: Boolean = false,
                          parent: Element = document.documentElement) {
        val path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${x.truncate()},${y.truncate()} L ${(x + width).truncate()},${y.truncate()}")
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_PROPERTY, stroke)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.truncate())
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.SVG_ROUND_VALUE)
        if (id != null) {
            path.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        }
        if (hidden) {
            path.setAttributeNS(null, SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE)
        }
        parent.appendChild(path)
    }

    /**
     * Adds a rectangle to the document. If there is no fill or stroke width provided, the rectangle
     * will be invisible but can be used to mark regions for filling in
     *
     * @param x The x coordinate of the top left
     * @param y The y coordinate of the top left
     * @param width The rectangle width
     * @param height The rectangle height
     * @param fill The color for internal region
     * @param id The id to assign to the element
     * @param strokeWidth The width of the stroke to outline the rectangle. If null, no outline will be drawn
     * @param stroke The color to use for the outline if strokeWidth is not null
     * @param parent The parent element for the rectangle
     */
    fun addRect(x: Double, y: Double, width: Double, height: Double,
                fill: String = SVGConstants.SVG_NONE_VALUE,
                id: String? = null, strokeWidth: Double? = null,
                stroke: String = FILL_DARK_GREY, parent: Element = document.documentElement) {
        val rect = document.createElementNS(svgNS, SVGConstants.SVG_RECT_TAG)
        rect.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, x.truncate())
        rect.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, y.truncate())
        rect.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, width.truncate())
        rect.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, height.truncate())
        rect.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, fill)
        if (id != null) {
            rect.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
        }
        if (strokeWidth != null) {
            rect.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, stroke)
            rect.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, strokeWidth.truncate())
        }
        parent.appendChild(rect)
    }

    fun createTranslatedGroup(x: Double, y: Double): Element {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        g.setAttributeNS(null, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
            "${SVGConstants.SVG_TRANSLATE_VALUE} (${x.truncate()} ${y.truncate()})")
        return g
    }

    fun addAeroMovementCompass(rect: Cell, parent: Element = document.documentElement) {
        val g = createTranslatedGroup(rect.x, rect.y)
        val fontSize = 9.35f
        val lineHeight = calcFontHeight(fontSize)
        val path = document.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG)
        path.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE)
        path.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, FILL_DARK_GREY)
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_WIDTH_PROPERTY, "2.9")
        path.setAttributeNS(null, SVGConstants.CSS_STROKE_LINEJOIN_PROPERTY, SVGConstants.CSS_BEVEL_VALUE)
        val hexRadius = (rect.height - lineHeight * 2.0) / sqrt(3.0) // distance from center to each vertex
        val hexDY = (rect.height - lineHeight * 2.0) * 0.5 // the delta y between the center and the top and bottom
        path.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
            "M ${(-hexRadius * 0.5).truncate()},${(-hexDY).truncate()} L ${(-hexRadius).truncate()},0"
                + " L ${(-hexRadius * 0.5).truncate()},${hexDY.truncate()} L ${(hexRadius * 0.5).truncate()},${hexDY.truncate()}"
                + " L ${hexRadius.truncate()},0 L ${(hexRadius * 0.5).truncate()},${(-hexDY).truncate()} Z")
        val pathTransform = createTranslatedGroup(rect.width * 0.75, rect.height * 0.5)
        pathTransform.appendChild(path)
        g.appendChild(pathTransform)
        addTextElement(rect.width * 0.75, rect.height * 0.5 - hexDY - 3.0, "A",
            fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(rect.width * 0.75 + hexRadius, rect.height * 0.5 - hexDY * 0.5 - 1.0, "B",
            fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(rect.width * 0.75 + hexRadius, rect.height * 0.5 + hexDY * 0.5 + 5.0, "C",
            fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(rect.width * 0.75, rect.height * 0.5 + hexDY + lineHeight - 1.0, "D",
            fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(rect.width * 0.75 - hexRadius, rect.height * 0.5 + hexDY * 0.5 + 5.0, "E",
            fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        addTextElement(rect.width * 0.75 - hexRadius, rect.height * 0.5 - hexDY * 0.5 - 1.0, "F",
            fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)

        val words = bundle.getString("advancedMovementCompass").split(" ")
        val firstLineY = (rect.height + lineHeight) * 0.5 - (lineHeight * words.size - 1) * 0.5
        for (word in words.withIndex()) {
            addTextElement(rect.width * 0.25, firstLineY + lineHeight * word.index , word.value,
                fontSize, SVGConstants.SVG_BOLD_VALUE, FILL_DARK_GREY, SVGConstants.SVG_MIDDLE_VALUE, parent = g)
        }
        parent.appendChild(g)
    }

    /**
     * Creates the 0-30 heat scale
     *
     * @param height The height of the region
     * @return A group of the heat scale elements
     */
    fun createHeatScale(height: Double): Element {
        val g = document.createElementNS(svgNS, SVGConstants.SVG_G_TAG)
        val lineHeight = calcFontHeight(FONT_SIZE_MEDIUM).toDouble()
        val boxHeight = (height - lineHeight * 3 - padding) / 33.0
        val boxWidth = boxHeight * 13.0 / 8.0 // close enough approximation of the golden ratio
        val textOffset = (boxHeight + lineHeight) * 0.5 - 1.0
        var ypos = lineHeight
        addTextElement(boxWidth * 0.5, ypos, bundle.getString("heatScale.1"), FONT_SIZE_MEDIUM,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, fixedWidth = true, parent = g)
        ypos += lineHeight
        addTextElement(boxWidth * 0.5, ypos, bundle.getString("heatScale.2"), FONT_SIZE_MEDIUM,
            SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, fixedWidth = true, parent = g)
        ypos += lineHeight
        g.appendChild(RoundedBorder(boxWidth * 0.5 - boxHeight, ypos - 1.5, boxHeight * 2, boxHeight * 2,
            4.35, 2.4, 1.45).draw(document))
        addTextElement(boxWidth * 0.5, ypos + 4.5, bundle.getString("overflow"),
            4.7f, anchor = SVGConstants.SVG_MIDDLE_VALUE, width = 1.5 * boxHeight, parent = g)
        ypos += boxHeight * 2 + padding
        for (i in 30 downTo 0) {
            addRect(0.0, ypos, boxWidth, boxHeight, fill = if (i < 10) {
                // gradient green -> yellow
                format("#%xffcc", 0xcc + i * (0xff - 0xcc) / 10)
            } else {
                // gradient yellow -> red
                format("#ff%xcc", 0xff - (i - 10) * (0xff - 0xcc) / 20)
            }, strokeWidth = 1.45, parent = g)
            addTextElement(boxWidth * 0.5, ypos + textOffset,
                if (heatEffect(i) == null) i.toString() else "$i*", FONT_SIZE_MEDIUM,
                SVGConstants.SVG_BOLD_VALUE, anchor = SVGConstants.SVG_MIDDLE_VALUE, parent = g)
            ypos += boxHeight
        }

        return g
    }

    fun addHeatEffects(x: Double, y: Double, width: Double, height: Double, parent: Element) {
        val levelX = x + padding * 4
        val textX = levelX + padding * 4
        val pipX = x + width - padding * 2 - 30
        val textWidth = pipX - textX - padding
        val effects = HashMap<Int, String>()
        val effects2 = HashMap<Int, String>()
        for (heat in 30 downTo 0) {
            val text = heatEffect(heat)
            if (text != null) {
                effects[heat] = text
                if (calcTextLength(text, FONT_SIZE_MEDIUM) > textWidth) {
                    var joinIndex = text.indexOf("/")
                    if (joinIndex < 0) {
                        joinIndex = text.indexOf(",") + 1
                    }
                    if (joinIndex >= 0) {
                        effects[heat] = text.substring(0, joinIndex)
                        effects2[heat] = text.substring(joinIndex)
                    }
                }
            }
        }
        val lineHeight = (height - padding * 2) / (effects.size + effects2.size + 2)
        var ypos = y + lineHeight
        addTextElement(levelX, ypos, bundle.getString("heatLevel.1"), FONT_SIZE_MEDIUM,
            anchor = SVGConstants.SVG_MIDDLE_VALUE, fixedWidth = true, parent = parent)
        ypos += lineHeight
        addTextElement(levelX, ypos, bundle.getString("heatLevel.2"), FONT_SIZE_MEDIUM,
            anchor = SVGConstants.SVG_MIDDLE_VALUE, fixedWidth = true, parent = parent)
        addTextElement(textX + textWidth * 0.3, ypos, bundle.getString("effects"), FONT_SIZE_MEDIUM,
            anchor = SVGConstants.SVG_MIDDLE_VALUE, fixedWidth = true, parent = parent)
        ypos += lineHeight
        for (heat in 30 downTo 0) {
            if (heat in effects) {
                addTextElement(levelX, ypos, heat.toString(), FONT_SIZE_MEDIUM,
                    anchor = SVGConstants.SVG_MIDDLE_VALUE, fixedWidth = true, parent = parent)
                addTextElement(textX, ypos, effects[heat]!!, FONT_SIZE_MEDIUM,
                    anchor = SVGConstants.SVG_START_VALUE, fixedWidth = true, parent = parent)
                ypos += lineHeight
                if (heat in effects2) {
                    addTextElement(textX + padding, ypos, effects2[heat]!!, FONT_SIZE_MEDIUM,
                        anchor = SVGConstants.SVG_START_VALUE, fixedWidth = true, parent = parent)
                    ypos += lineHeight
                }
            }
        }

        addTextElement(x + width - padding * 4, y + 4, bundle.getString("heatSinks"), 8.44f,
            anchor = SVGConstants.SVG_END_VALUE, id= "hsType", parent = parent)
        addTextElement(x + width - padding * 4, y + 13, "10", 8.44f,
            anchor = SVGConstants.SVG_END_VALUE, id= "hsCount", parent = parent)
        if (this is LAMRecordSheet) {
            addTextElement(x + width - padding * 4, y + 19, ((this as RecordSheet).bundle).getString("airmechHeat"), 5.8f,
                anchor = SVGConstants.SVG_END_VALUE, fixedWidth = true, parent = parent)
        }
        addRect(x + width - padding * 2 - 30, y + 24, 30.0,height - y - 24, id = "heatSinkPips",
            parent = parent)
    }
}