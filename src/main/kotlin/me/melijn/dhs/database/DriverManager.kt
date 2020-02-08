package me.melijn.dhs.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.melijn.dhs.objects.Settings
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class DriverManager(dbSettings: Settings.Database) {


    private val afterConnectToBeExecutedQueries = ArrayList<String>()
    private val dataSource: DataSource
    private val logger = LoggerFactory.getLogger(DriverManager::class.java.name)


    init {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${dbSettings.host}:${dbSettings.port}/${dbSettings.database}"
        config.username = dbSettings.user
        config.password = dbSettings.password

        config.maxLifetime = 30_000
        config.validationTimeout = 3_000
        config.connectionTimeout = 30_000
        config.leakDetectionThreshold = 3_000
        config.maximumPoolSize = 3

        this.dataSource = HikariDataSource(config)
    }


    fun getUsableConnection(connection: (Connection) -> Unit) {
        val startConnection = System.currentTimeMillis()
        dataSource.connection.use { connection.invoke(it) }
        if (System.currentTimeMillis() - startConnection > 3_000) {
            logger.info("Connection collected: Alive for ${(System.currentTimeMillis() - startConnection)}ms")
        }
    }


    fun registerTable(table: String, tableStructure: String, primaryKey: String, uniqueKey: String = "") {
        val hasPrimary = primaryKey != ""
        val hasUnique = uniqueKey != ""
        afterConnectToBeExecutedQueries.add(
            "CREATE TABLE IF NOT EXISTS $table ($tableStructure${if (hasPrimary) {
                ", PRIMARY KEY ($primaryKey)"
            } else {
                ""
            }}${if (hasUnique) ", UNIQUE ($uniqueKey)" else ""})"
        )
    }


    fun executeTableRegistration() {
        getUsableConnection { connection ->
            connection.createStatement().use { statement ->
                afterConnectToBeExecutedQueries.forEach { tableRegistrationQuery ->
                    statement.addBatch(tableRegistrationQuery)
                }
                statement.executeBatch()
            }
        }
    }


    /** returns the amount of rows affected by the query
     * [query] the sql query that needs execution
     * [objects] the arguments of the query
     * [Int] returns the amount of affected rows
     * example:
     *   query: "UPDATE apples SET bad = ? WHERE id = ?"
     *   objects: true, 6
     *   return value: 1
     * **/
    suspend fun executeUpdate(query: String, vararg objects: Any?): Int = suspendCoroutine {
        try {
            getUsableConnection { connection ->
                connection.prepareStatement(query).use { preparedStatement ->
                    for ((index, value) in objects.withIndex()) {
                        preparedStatement.setObject(index + 1, value)
                    }
                    val rows = preparedStatement.executeUpdate()
                    it.resume(rows)
                }
            }
        } catch (e: SQLException) {
            logger.error("Something went wrong when executing the query: $query\nObjects: ${objects.joinToString { o -> o.toString() }}")
            e.printStackTrace()
        }
    }


    /**
     * [query] the sql query that needs execution
     * [resultset] The consumer that will contain the resultset after executing the query
     * [objects] the arguments of the query
     * example:
     *   query: "SELECT * FROM apples WHERE id = ?"
     *   objects: 5
     *   resultset: Consumer object to handle the resultset
     * **/
    fun executeQuery(query: String, resultset: (ResultSet) -> Unit, vararg objects: Any?) {
        try {
            getUsableConnection { connection ->
                if (connection.isClosed) {
                    logger.warn("Connection closed: $query")
                }
                connection.prepareStatement(query).use { preparedStatement ->
                    for ((index, value) in objects.withIndex()) {
                        preparedStatement.setObject(index + 1, value)
                    }
                    preparedStatement.executeQuery().use { resultSet -> resultset.invoke(resultSet) }
                }
            }
        } catch (e: SQLException) {
            logger.error("Something went wrong when executing the query: $query\n" +
                "Objects: ${objects.joinToString { o -> o.toString() }}")
            e.printStackTrace()
        }
    }


    fun clear(table: String): Int {
        dataSource.connection.use { connection ->
            connection.prepareStatement("TRUNCATE $table").use { preparedStatement ->
                return preparedStatement.executeUpdate()
            }
        }
    }


    fun dropTable(table: String) {
        afterConnectToBeExecutedQueries.add(0, "DROP TABLE $table")
    }
}