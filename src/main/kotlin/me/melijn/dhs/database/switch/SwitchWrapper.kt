package me.melijn.dhs.database.switch

import me.melijn.dhs.objects.components.SwitchComponent

class SwitchWrapper(private val switchDao: SwitchDao) {

    suspend fun getSwitches(): List<SwitchComponent> {
        return switchDao.getSwitches()
    }
}