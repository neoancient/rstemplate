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
    // Mechs
    outputRS(BipedMechRecordSheet(size), dir)
    outputRS(QuadMechRecordSheet(size), dir)
    outputRS(TripodMechRecordSheet(size), dir)
    outputRS(LAMRecordSheet(size), dir)
    outputRS(QuadVeeRecordSheet(size), dir)
    outputRS(BipedMechTOHeatRecordSheet(size), dir)
    outputRS(QuadMechTOHeatRecordSheet(size), dir)
    outputRS(TripodMechTOHeatRecordSheet(size), dir)
    outputRS(LAMTOHeatRecordSheet(size), dir)
    outputRS(QuadVeeTOHeatRecordSheet(size), dir)
    // Vehicles
    outputRS(NoTurretVehicleRecordSheet(size), dir)
    outputRS(SingleTurretVehicleRecordSheet(size), dir)
    outputRS(DualTurretVehicleRecordSheet(size), dir)
    outputRS(NoTurretSHVehicleRecordSheet(size), dir)
    outputRS(SingleTurretSHVehicleRecordSheet(size), dir)
    outputRS(DualTurretSHVehicleRecordSheet(size), dir)
    outputRS(VTOLRecordSheet(size), dir)
    outputRS(VTOLTurretRecordSheet(size), dir)
    outputRS(WiGESingleTurretRecordSheet(size), dir)
    outputRS(WiGENoTurretRecordSheet(size), dir)
    outputRS(WiGEDualTurretRecordSheet(size), dir)
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
    // Aerospace
    outputRS(ASFRecordSheet(size), dir)
    outputRS(ConvFighterRecordSheet(size), dir)
    outputRS(AerodyneSmallCraftRecordSheet(size), dir)
    outputRS(SpheroidSmallCraftRecordSheet(size), dir)
    outputRS(AerodyneDropshipRecordSheet(size), dir)
    outputRS(SpheroidDropshipRecordSheet(size), dir)
    outputRS(JumpshipRecordSheet(size), dir)
    outputRS(WarshipRecordSheet(size), dir)
    outputRS(SpaceStationRecordSheet(size), dir)
    // other
    outputRS(InfantryRecordSheet(size), dir)
    outputRS(InfantryMultiSheet(size), dir)
    outputRS(InfantryMultiSheetTables(size), dir)
    outputRS(BattleArmorRecordSheet(size), dir)
    outputRS(BAMultiSheet(size), dir)
    outputRS(BipedProtomechRecordSheet(size), dir)
    outputRS(QuadProtomechRecordSheet(size), dir)
    outputRS(GliderProtomechRecordSheet(size), dir)
    outputRS(ProtoMechMultiSheet(size), dir)
}

private fun outputRS(sheet: RecordSheet, dir: String) {
    sheet.build()
    val ostr = FileOutputStream(File(dir, sheet.fileName))
    sheet.export(ostr)
    ostr.close()
}