package org.megamek.rstemplate

import org.junit.jupiter.api.Test
import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.templates.BipedMechRecordSheet
import java.io.File
import java.io.FileOutputStream

/**
 *
 */
internal class MechRecordSheetTest {
    @Test
    fun testMechRecordSheet() {
        val sheet = BipedMechRecordSheet(PaperSize.LETTER, true)

        val ostr = FileOutputStream(File("record_sheet_test.svg"))
        sheet.export(ostr)
        ostr.close()
    }
}