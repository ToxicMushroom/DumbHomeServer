package me.melijn.dhs.database.log

class LogWrapper(val logDao: LogDao) {

    suspend fun log(user: String, action: String) {
        logDao.log(user, action, System.currentTimeMillis())
    }
}