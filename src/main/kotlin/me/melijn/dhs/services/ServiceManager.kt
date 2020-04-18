package me.melijn.dhs.services

import me.melijn.dhs.database.CacheManager
import me.melijn.dhs.objects.Settings
import me.melijn.dhs.services.chacon.ChaconService
import me.melijn.dhs.services.chacon.LogService

class ServiceManager(settings: Settings, cacheManager: CacheManager) {

    var started = false

    val services = mutableListOf(
        ChaconService(cacheManager, settings.location),
        LogService(cacheManager.dbManager.logWrapper, settings.logDays)
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