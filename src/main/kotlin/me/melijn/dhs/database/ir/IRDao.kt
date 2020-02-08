package me.melijn.dhs.database.ir

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager

class IRDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "remote_buttons"
    override val tableStructure: String = "id varchar(64), codeSequence varchar(1024)"
    override val primaryKey: String = "id"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

}