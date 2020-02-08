package me.melijn.dhs.database.preset

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager

class ButtonPresetDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "button_presets"
    override val tableStructure: String = "username varchar(32), name varchar(32), id int"
    override val primaryKey: String = "username, name, id"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }
}