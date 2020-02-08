package me.melijn.dhs.database.log

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager
import me.melijn.dhs.objects.DAY_IN_MILLIS

class LogDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "logs"
    override val tableStructure: String = "username varchar(32), action varchar(512), moment bigint"
    override val primaryKey: String = "moment"


    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun deleteOldLogs(logDays: Int) {
        driverManager.executeUpdate("DELETE FROM $table WHERE moment < ?",
            System.currentTimeMillis() - logDays * DAY_IN_MILLIS
        )
    }

    suspend fun log(user: String, action: String, moment: Long) {
        driverManager.executeUpdate("INSERT INTO $table (username, action, moment) VALUES (?, ?, ?)",
            user, action, moment)
    }
}