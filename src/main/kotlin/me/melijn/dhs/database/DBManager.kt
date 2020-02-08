package me.melijn.dhs.database

import me.melijn.dhs.database.log.LogWrapper
import me.melijn.dhs.database.preset.SwitchPresetWrapper
import me.melijn.dhs.database.service.SwitchServiceWrapper
import me.melijn.dhs.database.switch.SwitchWrapper
import me.melijn.dhs.database.user.UserWrapper
import me.melijn.dhs.objects.Settings

class DBManager(dbSettings: Settings.Database) {

    companion object {
        val afterTableFunctions = mutableListOf<() -> Unit>()
    }


    var driverManager: DriverManager = DriverManager(dbSettings)
    val daoManager = DaoManager(driverManager)

    val logWrapper = LogWrapper(daoManager.logDao)
    val userWrapper = UserWrapper(daoManager.userDao)

    val switchWrapper = SwitchWrapper(daoManager.switchDao)
    val switchServiceWrapper = SwitchServiceWrapper(daoManager.switchServiceDao)
    val switchPresetWrapper = SwitchPresetWrapper(daoManager.switchPresetDao)


    init {
        driverManager.executeTableRegistration()
        for (func in afterTableFunctions) {
            func()
        }
    }
}