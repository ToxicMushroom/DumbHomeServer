package me.melijn.dhs.database.button

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager

class ButtonDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "buttons"
    override val tableStructure: String = "id int, name varchar(32), url varchar(128), location varchar(64)"
    override val primaryKey: String = "id"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }
}
