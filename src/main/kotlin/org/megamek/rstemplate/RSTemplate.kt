package org.megamek.rstemplate

import org.megamek.rstemplate.layout.PaperSize
import java.io.File
import java.io.FileOutputStream

/**
 * Application main function
 */
fun main() {
    writeRecordSheets(PaperSize.LETTER, "templates_us")
    writeRecordSheets(PaperSize.LETTER, "templates_iso")
}

fun writeRecordSheets(size: PaperSize, dir: String) {
    if (!File(dir).exists()) {
        File(dir).mkdir()
    }
    outputRS(BipedMechRecordSheet(size), dir)
    outputRS(QuadMechRecordSheet(size), dir)
    outputRS(TripodMechRecordSheet(size), dir)
    outputRS(LAMRecordSheet(size), dir)
    outputRS(QuadVeeRecordSheet(size), dir)
}

private fun outputRS(sheet: RecordSheet, dir: String) {
    val ostr = FileOutputStream(File(dir, sheet.fileName))
    sheet.export(ostr)
    ostr.close()
}