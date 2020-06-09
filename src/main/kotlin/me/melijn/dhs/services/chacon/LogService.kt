package me.melijn.dhs.services.chacon

import me.melijn.dhs.database.log.LogWrapper
import me.melijn.dhs.services.Service
import me.melijn.dhs.threading.RunnableTask
import java.util.concurrent.TimeUnit

class LogService(logWrapper: LogWrapper, logDays: Int) :
    Service("Log", 1, unit = TimeUnit.HOURS) {

    override val service = RunnableTask {
        logWrapper.deleteOldLogs(logDays)
    }
}