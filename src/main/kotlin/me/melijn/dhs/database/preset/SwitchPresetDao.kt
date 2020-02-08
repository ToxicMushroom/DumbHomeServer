package me.melijn.dhs.database.preset

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager
import me.melijn.dhs.objects.components.Location
import me.melijn.dhs.objects.components.SwitchComponent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SwitchPresetDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "switch_presets"
    override val tableStructure: String = "username varchar(32), name varchar(32), id int"
    override val primaryKey: String = "username, name, id"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun getSwitchesByUsername(username: String): List<SwitchComponent> = suspendCoroutine {
        val switchComponents = mutableListOf<SwitchComponent>()
        driverManager.executeQuery("SELECT * FROM $table WHERE username = ?", { rs ->
            while (rs.next()) {
                switchComponents.add(SwitchComponent(
                    rs.getString("name"),
                    Location.valueOf(rs.getString("location")),
                    rs.getInt("id"),
                    rs.getBoolean("last_state"),
                    rs.getInt("on_code"),
                    rs.getInt("off_code")
                ))
            }
            it.resume(switchComponents)
        }, username)
    }
}
