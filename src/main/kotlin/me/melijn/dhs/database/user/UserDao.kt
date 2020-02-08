package me.melijn.dhs.database.user

import me.melijn.dhs.database.Dao
import me.melijn.dhs.database.DriverManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserDao(driverManager: DriverManager) : Dao(driverManager) {

    override val table: String = "users"
    override val tableStructure: String = "username varchar(32), token varchar(64), access boolean"
    override val primaryKey: String = "username"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun getUserTokens(): Map<String, String> = suspendCoroutine {
        driverManager.executeQuery("SELECT * FROM $table WHERE access = ?", { rs ->
            val map = mutableMapOf<String, String>()
            while (rs.next()) {
                map[rs.getString("username")] = rs.getString("token")
            }

            it.resume(map)
        }, true)
    }
}