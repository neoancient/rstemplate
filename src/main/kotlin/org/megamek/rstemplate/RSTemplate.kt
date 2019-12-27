package org.megamek.rstemplate

import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.templates.*
import java.io.File
import java.io.FileOutputStream

/**
 * Application main function
 */
fun main() {
    writeRecordSheets(PaperSize.LETTER, "templates_us")
    writeRecordSheets(PaperSize.A4, "templates_iso")
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
    outputRS(NoTurretVehicleRecordSheet(size), dir)
    outputRS(SingleTurretVehicleRecordSheet(size), dir)
    outputRS(DualTurretVehicleRecordSheet(size), dir)
    outputRS(NoTurretSHVehicleRecordSheet(size), dir)
    outputRS(SingleTurretSHVehicleRecordSheet(size), dir)
    outputRS(DualTurretSHVehicleRecordSheet(size), dir)
    outputRS(VTOLRecordSheet(size), dir)
    outputRS(VTOLTurretRecordSheet(size), dir)
    outputRS(NavalNoTurretRecordSheet(size), dir)
    outputRS(NavalTurretRecordSheet(size), dir)
    outputRS(NavalDualTurretRecordSheet(size), dir)
    outputRS(SubmarineNoTurretRecordSheet(size), dir)
    outputRS(SubmarineTurretRecordSheet(size), dir)
    outputRS(SubmarineDualTurretRecordSheet(size), dir)
    outputRS(SHNavalNoTurretRecordSheet(size), dir)
    outputRS(SHNavalTurretRecordSheet(size), dir)
    outputRS(SHNavalDualTurretRecordSheet(size), dir)
    outputRS(SHSubmarineNoTurretRecordSheet(size), dir)
    outputRS(SHSubmarineTurretRecordSheet(size), dir)
    outputRS(SHSubmarineDualTurretRecordSheet(size), dir)
    outputRS(TankTables(size), dir)
    outputRS(VTOLTables(size), dir)
}

private fun outputRS(sheet: RecordSheet, dir: String) {
    sheet.build()
    val ostr = FileOutputStream(File(dir, sheet.fileName))
    sheet.export(ostr)
    ostr.close()
}