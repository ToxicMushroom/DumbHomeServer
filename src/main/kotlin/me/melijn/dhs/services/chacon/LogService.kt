package me.melijn.dhs.services.chacon

import kotlinx.coroutines.runBlocking
import me.melijn.dhs.database.log.LogWrapper
import me.melijn.dhs.services.Service
import java.util.concurrent.TimeUnit

class LogService(logWrapper: LogWrapper, logDays: Int) : Service("Log", 1, unit = TimeUnit.HOURS) {

    override val service = Runnable {
        runBlocking {
            logWrapper.deleteOldLogs(logDays)
        }
    }
}