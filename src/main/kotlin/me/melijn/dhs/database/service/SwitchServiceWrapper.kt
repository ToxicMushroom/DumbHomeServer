package me.melijn.dhs.database.service

class SwitchServiceWrapper(private val switchServiceDao: SwitchServiceDao) {

    suspend fun getEnabledSwitchServices(): List<SwitchServiceAction> {
        return switchServiceDao.getEnabledSwitchServices()
    }
}