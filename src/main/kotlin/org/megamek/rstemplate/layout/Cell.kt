package org.megamek.rstemplate.layout

/**
 *
 */
interface Cell {
    fun row(): Int
    fun column(): Int
    fun width(): Int
    fun height(): Int
}

