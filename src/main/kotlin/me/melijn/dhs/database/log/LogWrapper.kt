package me.melijn.dhs.database.log

class LogWrapper(private val logDao: LogDao) {

    suspend fun log(user: String, action: String) {
        logDao.log(user, action, System.nanoTime())
    }

    suspend fun deleteOldLogs(logDays: Int) {
        logDao.deleteOldLogs(logDays)
    }
}