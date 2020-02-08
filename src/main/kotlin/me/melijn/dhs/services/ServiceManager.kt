package me.melijn.dhs.services

import bot.zerotwo.helper.threading.TaskManager
import me.melijn.dhs.database.DBManager
import me.melijn.dhs.objects.Settings
import me.melijn.dhs.services.chacon.ChaconService

class ServiceManager(val taskManager: TaskManager, settings: Settings, dbManager: DBManager) {

    var started = false
    private val services = mutableListOf<Service>()

    init {
        services.add(
            ChaconService(settings.location, dbManager)
        )
    }

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