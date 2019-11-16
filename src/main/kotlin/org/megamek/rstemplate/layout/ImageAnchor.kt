package org.megamek.rstemplate.layout

/**
 * Used as a parameter for defining an image anchor and calculate the x/y offset given the width or
 * height of the area to place the image in and the width or height of the image.
 */
enum class ImageAnchor(val xOffset: (Double, Double) -> Double, val yOffset: (Double, Double) -> Double) {
    TOP_LEFT ({areaW, imageW -> 0.0}, {areaH, imageH -> 0.0}),
    TOP ({areaW, imageW -> (areaW - imageW) * 0.5}, {areaH, imageH -> 0.0}),
    TOP_RIGHT ({areaW, imageW -> areaW - imageW}, {areaH, imageH -> 0.0}),
    LEFT ({areaW, imageW -> 0.0}, {areaH, imageH -> (areaH - imageH) * 0.5}),
    CENTER ({areaW, imageW -> (areaW - imageW) * 0.5}, {areaH, imageH -> (areaH - imageH) * 0.5}),
    RIGHT ({areaW, imageW -> areaW - imageW}, {areaH, imageH -> (areaH - imageH) * 0.5}),
    BOTTOM_LEFT ({areaW, imageW -> 0.0}, {areaH, imageH -> areaH - imageH}),
    BOTTOM ({areaW, imageW -> (areaW - imageW) * 0.5}, {areaH, imageH -> areaH - imageH}),
    BOTTOM_RIGHT ({areaW, imageW -> areaW - imageW}, {areaH, imageH -> areaH - imageH})
}