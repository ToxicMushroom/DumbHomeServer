package me.melijn.dhs.database.service

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager
import me.melijn.dhs.utils.splitIETEL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SwitchServiceDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "switch_services"
    override val tableStructure: String = "triggerId int, codes varchar(128), days varchar(128), time varchar(64), enabled boolean"
    override val primaryKey: String = "triggerId"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun getEnabledSwitchServices(): List<SwitchServiceAction> = suspendCoroutine {
        driverManager.executeQuery("SELECT * FROM $table WHERE enabled = ?", { rs ->
            val list = mutableListOf<SwitchServiceAction>()

            while (rs.next()) {
                list.add(
                    SwitchServiceAction(
                        rs.getInt("triggerId"),
                        rs.getString("codes")
                            .splitIETEL(",")
                            .map { it.toInt() },
                        rs.getString("days")
                            .splitIETEL(",")
                            .map { it.toInt() },
                        rs.getString("time"),
                        rs.getBoolean("enabled")
                    )
                )
            }
            it.resume(list)
        }, true)
    }

}

class SwitchServiceAction(
    val triggerId: Int,
    val codes: List<Int>,
    val days: List<Int>,
    val time: String,
    val state: Boolean
)