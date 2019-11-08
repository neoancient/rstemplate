package org.megamek.rstemplate.layout

/**
 * Convenience class for tracking and manipulating a rectangular section of a record sheet.
 */
data class Cell(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
) {
    fun translate(dx: Double, dy: Double) = Cell(x + dx, y + dy, width, height)

    fun scale(sx: Double, sy: Double) = Cell(x, y, width * sx, height * sy);

    fun inset(left: Double, right: Double, top: Double, bottom: Double) =
        Cell(x + left, y + top, width - left - right, height - top - bottom)

    fun rightX() = x + width

    fun bottomY() = y + height
}

