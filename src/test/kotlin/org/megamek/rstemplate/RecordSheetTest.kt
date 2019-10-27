package org.megamek.rstemplate

import org.junit.jupiter.api.Test
import org.megamek.rstemplate.layout.PaperSize

/**
 *
 */
internal class RecordSheetTest {

    @Test
    fun embedImage() {
        val sheet = RecordSheet(PaperSize.LETTER)

        sheet.export()
    }
}