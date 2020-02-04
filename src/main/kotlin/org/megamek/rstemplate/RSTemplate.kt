package org.megamek.rstemplate

import org.megamek.rstemplate.layout.PaperSize
import org.megamek.rstemplate.templates.*
import java.io.File
import java.io.FileOutputStream

/**
 * Application main function
 */
fun main() {
    writeRecordSheets(PaperSize.LETTER, true, "templates_us")
    writeRecordSheets(PaperSize.LETTER, false,"templates_bw_us")
    writeRecordSheets(PaperSize.A4, true, "templates_iso")
    writeRecordSheets(PaperSize.A4, false, "templates_bw_iso")
}

fun writeRecordSheets(size: PaperSize, color: Boolean, dir: String) {
    if (!File(dir).exists()) {
        File(dir).mkdir()
    }
    // Mechs
    outputRS(BipedMechRecordSheet(size, color), dir)
    outputRS(QuadMechRecordSheet(size, color), dir)
    outputRS(TripodMechRecordSheet(size, color), dir)
    outputRS(LAMRecordSheet(size, color), dir)
    outputRS(QuadVeeRecordSheet(size, color), dir)
    // Vehicles
    outputRS(NoTurretVehicleRecordSheet(size, color), dir)
    outputRS(SingleTurretVehicleRecordSheet(size, color), dir)
    outputRS(DualTurretVehicleRecordSheet(size, color), dir)
    outputRS(NoTurretSHVehicleRecordSheet(size, color), dir)
    outputRS(SingleTurretSHVehicleRecordSheet(size, color), dir)
    outputRS(DualTurretSHVehicleRecordSheet(size, color), dir)
    outputRS(VTOLRecordSheet(size, color), dir)
    outputRS(VTOLTurretRecordSheet(size, color), dir)
    outputRS(WiGESingleTurretRecordSheet(size, color), dir)
    outputRS(WiGENoTurretRecordSheet(size, color), dir)
    outputRS(WiGEDualTurretRecordSheet(size, color), dir)
    outputRS(NavalNoTurretRecordSheet(size, color), dir)
    outputRS(NavalTurretRecordSheet(size, color), dir)
    outputRS(NavalDualTurretRecordSheet(size, color), dir)
    outputRS(SubmarineNoTurretRecordSheet(size, color), dir)
    outputRS(SubmarineTurretRecordSheet(size, color), dir)
    outputRS(SubmarineDualTurretRecordSheet(size, color), dir)
    outputRS(SHNavalNoTurretRecordSheet(size, color), dir)
    outputRS(SHNavalTurretRecordSheet(size, color), dir)
    outputRS(SHNavalDualTurretRecordSheet(size, color), dir)
    outputRS(SHSubmarineNoTurretRecordSheet(size, color), dir)
    outputRS(SHSubmarineTurretRecordSheet(size, color), dir)
    outputRS(SHSubmarineDualTurretRecordSheet(size, color), dir)
    outputRS(TankTables(size, color), dir)
    outputRS(VTOLTables(size, color), dir)
    // Aerospace
    outputRS(ASFRecordSheet(size, color), dir)
    outputRS(ConvFighterRecordSheet(size, color), dir)
    outputRS(AerodyneSmallCraftRecordSheet(size, color), dir)
    outputRS(SpheroidSmallCraftRecordSheet(size, color), dir)
    outputRS(AerodyneDropshipRecordSheet(size, color), dir)
    outputRS(SpheroidDropshipRecordSheet(size, color), dir)
}

private fun outputRS(sheet: RecordSheet, dir: String) {
    sheet.build()
    val ostr = FileOutputStream(File(dir, sheet.fileName))
    sheet.export(ostr)
    ostr.close()
}