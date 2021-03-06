package me.melijn.dhs.database.preset

import me.melijn.dhs.objects.components.SwitchComponent

class SwitchPresetWrapper(private val switchPresetDao: SwitchPresetDao) {

    suspend fun getSwitchesByUsername(username: String): List<SwitchComponent> {
        return switchPresetDao.getSwitchesByUsername(username)
    }
}