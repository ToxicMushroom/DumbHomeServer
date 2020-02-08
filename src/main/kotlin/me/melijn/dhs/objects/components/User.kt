package me.melijn.dhs.objects.components

import me.melijn.dhs.database.DBManager

class User(val name: String, val token: String) {

    private val switchComponentOverrides = mutableListOf<SwitchComponent>()

    suspend fun initOverrides(database: DBManager) {
        switchComponentOverrides.addAll(
            database.switchPresetWrapper.getSwitchesByUsername(name)
        )
    }

    fun getSwitchComponentOverrides(): List<SwitchComponent> {
        return switchComponentOverrides
    }

}