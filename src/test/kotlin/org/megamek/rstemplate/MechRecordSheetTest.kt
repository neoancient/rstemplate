package org.megamek.rstemplate

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.megamek.rstemplate.layout.PaperSize

/**
 *
 */
internal class MechRecordSheetTest {
    @Test
    fun testMechRecordSheet() {
        val sheet = MechRecordSheet(PaperSize.LETTER)

        sheet.export()
    }
}