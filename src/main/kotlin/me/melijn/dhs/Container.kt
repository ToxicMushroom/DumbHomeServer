package me.melijn.dhs

import me.melijn.dhs.database.CacheManager
import me.melijn.dhs.database.DBManager
import me.melijn.dhs.objects.Settings
import me.melijn.dhs.rest.OBJECT_MAPPER
import me.melijn.dhs.services.ServiceManager
import me.melijn.dhs.threading.TaskManager
import java.io.File

class Container {

    var settings: Settings = OBJECT_MAPPER.readValue(File("config.json"), Settings::class.java)
    val taskManager = TaskManager()
    val dbManager = DBManager(settings.database)
    val cacheManager = CacheManager(dbManager)
    val serviceManager = ServiceManager(settings, cacheManager)
    val startTime = System.currentTimeMillis()

}