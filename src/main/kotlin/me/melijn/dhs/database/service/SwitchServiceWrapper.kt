package me.melijn.dhs.database.service

class SwitchServiceWrapper(val switchServiceDao: SwitchServiceDao) {

    suspend fun getEnabledSwitchServices(): List<SwitchServiceAction> {
        return switchServiceDao.getEnabledSwitchServices()
    }
}