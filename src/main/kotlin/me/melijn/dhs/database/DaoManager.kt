package me.melijn.dhs.database

import me.melijn.dhs.database.button.ButtonDao
import me.melijn.dhs.database.ir.IRDao
import me.melijn.dhs.database.log.LogDao
import me.melijn.dhs.database.preset.ButtonPresetDao
import me.melijn.dhs.database.preset.SwitchPresetDao
import me.melijn.dhs.database.preset.ViewPresetDao
import me.melijn.dhs.database.service.SwitchServiceDao
import me.melijn.dhs.database.switch.SwitchDao
import me.melijn.dhs.database.user.UserDao
import me.melijn.dhs.database.view.ViewDao

class DaoManager(driverManager: DriverManager) {

    val userDao = UserDao(driverManager)
    val logDao = LogDao(driverManager)

    val irDao = IRDao(driverManager)

    val buttonDao = ButtonDao(driverManager)
    val buttonPresetDao = ButtonPresetDao()

    val viewDao = ViewDao(driverManager)
    val viewPresetDao = ViewPresetDao(driverManager)

    val switchDao = SwitchDao(driverManager)
    val switchPresetDao = SwitchPresetDao(driverManager)
    val switchServiceDao = SwitchServiceDao(driverManager)
}