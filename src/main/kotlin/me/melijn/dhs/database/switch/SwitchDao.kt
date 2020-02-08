package me.melijn.dhs.database.switch

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager
import me.melijn.dhs.objects.components.Location
import me.melijn.dhs.objects.components.SwitchComponent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SwitchDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "switches"
    override val tableStructure: String = "id int, name varchar(32), on_code int, off_code int, location varchar(64), last_state boolean"
    override val primaryKey: String = "id"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun getSwitches(): List<SwitchComponent> = suspendCoroutine {
        val switchComponents = mutableListOf<SwitchComponent>()
        driverManager.executeQuery("SELECT * FROM $table", { rs ->
            while (rs.next()) {
                if (rs.getString("location").isEmpty()) continue
                switchComponents.add(
                    SwitchComponent(
                        rs.getString("name"),
                        Location.valueOf(rs.getString("location")),
                        rs.getInt("id"),
                        rs.getBoolean("last_state"),
                        rs.getInt("on_code"),
                        rs.getInt("off_code")
                    )
                )
            }
            it.resume(switchComponents)
        })
    }
}