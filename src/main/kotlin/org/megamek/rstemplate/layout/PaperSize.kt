package org.megamek.rstemplate.layout

/**
 * Standard paper sizes with width and height in pixels at 72 dpi
 */
enum class PaperSize (val width: Int, val height: Int) {
    LETTER (612, 792),
    LEGAL (612, 1008),
    A4 (595, 842)
}