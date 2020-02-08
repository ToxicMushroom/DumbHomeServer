package me.melijn.dhs.services

import me.melijn.dhs.database.DBManager
import me.melijn.dhs.objects.Settings
import me.melijn.dhs.services.chacon.ChaconService
import me.melijn.dhs.services.chacon.LogService

class ServiceManager(settings: Settings, dbManager: DBManager) {

    var started = false

    private val services = mutableListOf(
        ChaconService(dbManager, settings.location),
        LogService(dbManager.logWrapper, settings.logDays)
    )

    fun startServices() {
        services.forEach { service ->
            service.start()
            service.logger.info("Started ${service.name}Service")
        }
        started = true
    }

    fun stopServices() {
        require(started) { "Never started!" }
        services.forEach { service ->
            service.stop()
            service.logger.info("Stopped ${service.name}Service")
        }
    }
}